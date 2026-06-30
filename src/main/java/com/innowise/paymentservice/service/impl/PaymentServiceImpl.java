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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
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

        Status newStatus = processPayment();
        savedPayment.setStatus(newStatus);
        savedPayment.setTimestamp(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(savedPayment);

        PaymentCompletedEvent paymentCompletedEvent = new PaymentCompletedEvent(
                updatedPayment.getOrderId(),
                updatedPayment.getStatus().name()
        );

        kafkaTemplate.send(kafkaTopicsProperties.paymentEvents(),paymentCompletedEvent);

        return paymentMapper.toDto(updatedPayment);
    }

    @Override
    public PaymentOutputDto findById(String id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Payment with this id " + id + " not found")
        );

        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentOutputDto> findPayments(Long orderId, Status status, Long userId) {

        return findPaymentWithCriteria(userId, orderId, status)
                .stream()
                .map(paymentMapper::toDto)
                .toList();

    }

    @Override
    public PaymentSummaryResponse getUserSummary(Long userId, LocalDateTime from, LocalDateTime to) {

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("userId").is(userId)
                        .and("status").is(Status.SUCCESS)
                        .and("timestamp").gte(from).lte(to)
                ),

                Aggregation.group().sum("paymentAmount").as("total")
        );

        return getPaymentSummaryResponse(aggregation);
    }
    @Override
    public PaymentSummaryResponse  getGlobalSummary(LocalDateTime from,
                                               LocalDateTime to) {

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("status").is(Status.SUCCESS)
                                .and("timestamp").gte(from).lte(to)
                ),

                Aggregation.group().sum("paymentAmount").as("total")
        );

        return getPaymentSummaryResponse(aggregation);
    }

    private PaymentSummaryResponse getPaymentSummaryResponse(Aggregation aggregation) {
        AggregationResults<Document> result =
                mongoTemplate.aggregate(aggregation, "payments", Document.class);

        Document doc = result.getUniqueMappedResult();

        BigDecimal total = (doc == null || doc.get("total") == null)
                ? BigDecimal.ZERO
                : new BigDecimal(doc.get("total").toString());

        return new PaymentSummaryResponse(total);
    }

    private List<Payment> findPaymentWithCriteria(Long userId, Long orderId, Status status) {

        Query query = new Query();

        if (userId != null) {
            query.addCriteria(Criteria.where("user_id").is(userId));
        }

        if (orderId != null) {
            query.addCriteria(Criteria.where("order_id").is(orderId));
        }

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        return mongoTemplate.find(query, Payment.class);
    }

    private Status processPayment() {

        int number = Integer.parseInt(randomNumberClient.getRandomNumber().trim());

        return (number % 2 == 0)
                ? Status.SUCCESS
                : Status.FAILED;
    }

}
