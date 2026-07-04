package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.config.kafka.properties.KafkaTopicsProperties;
import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.dto.event.PaymentCompletedEvent;
import com.innowise.paymentservice.dto.input.PaymentInputDto;
import com.innowise.paymentservice.dto.output.PaymentOutputDto;
import com.innowise.paymentservice.exception.NotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.Status;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final RandomNumberClient randomNumberClient;

    @Override
    public PaymentOutputDto createPayment(PaymentInputDto paymentInputDto, Long userId) {

        Payment payment = Payment.builder()
                .orderId(paymentInputDto.orderId())
                .status(Status.PENDING)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .paymentAmount(paymentInputDto.paymentAmount())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        processPaymentAsync(savedPayment.getId());

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public PaymentOutputDto findById(String id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Payment with this id " + id + " not found")
        );

        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentOutputDto> findPayments(Long orderId, Status status, Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long effectiveUserId = isAdmin ? null : userId;

        return paymentRepository.findByCriteria(effectiveUserId, orderId, status)
                .stream()
                .map(paymentMapper::toDto)
                .toList();

    }

    @Override
    public PaymentSummaryResponse getUserSummary(Long userId, LocalDateTime from, LocalDateTime to) {

        Aggregation aggregation = buildSummaryAggregation(userId, from, to);

        return getPaymentSummaryResponse(aggregation);
    }
    @Override
    public PaymentSummaryResponse getGlobalSummary(LocalDateTime from,
                                               LocalDateTime to) {

        Aggregation aggregation = buildSummaryAggregation(null, from, to);

        return getPaymentSummaryResponse(aggregation);
    }

    private PaymentSummaryResponse getPaymentSummaryResponse(Aggregation aggregation) {
        AggregationResults<Document> result =
                mongoTemplate.aggregate(aggregation, "payments", Document.class);

        Document doc = result.getUniqueMappedResult();

        BigDecimal total = BigDecimal.ZERO;

        if (doc != null && doc.get("total") != null) {
            total = new BigDecimal(doc.get("total").toString());
        }

        return new PaymentSummaryResponse(total);
    }

    private Status processPayment() {

        int number = Integer.parseInt(randomNumberClient.getRandomNumber().trim());

        return (number % 2 == 0)
                ? Status.SUCCESS
                : Status.FAILED;
    }

    @Async
    public void processPaymentAsync(String id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow();

        Status newStatus = processPayment();

        payment.setStatus(newStatus);
        payment.setTimestamp(LocalDateTime.now());

        Payment updated = paymentRepository.save(payment);

        kafkaTemplate.send(
                kafkaTopicsProperties.paymentEvents(),
                new PaymentCompletedEvent(
                        updated.getOrderId(),
                        updated.getStatus().name()
                )
        );
    }

    private Aggregation buildSummaryAggregation(Long userId, LocalDateTime from, LocalDateTime to) {

        Criteria criteria = Criteria.where("status").is(Status.SUCCESS)
                .and("timestamp").gte(from).lte(to);

        if (userId != null) {
            criteria = criteria.and("user_id").is(userId);
        }

        return Aggregation.newAggregation(

                Aggregation.match(criteria),

                Aggregation.addFields()
                        .addFieldWithValue(
                                "payment_amount_decimal",
                                ConvertOperators.Convert.convertValue("$payment_amount").to("decimal")
                        )
                        .build(),

                Aggregation.group()
                        .sum("payment_amount_decimal")
                        .as("total")
        );
    }
}
