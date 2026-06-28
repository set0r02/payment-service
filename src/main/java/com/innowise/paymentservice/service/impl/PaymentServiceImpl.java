package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.dto.input.PaymentInputDto;
import com.innowise.paymentservice.dto.output.PaymentOutputDto;
import com.innowise.paymentservice.exception.NotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.Status;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentOutputDto createPayment(PaymentInputDto paymentInputDto, Long userId) {

        Payment payment = Payment.builder()
                .orderId(paymentInputDto.orderId())
                .status(Status.PENDING)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .paymentAmount(paymentInputDto.paymentAmount())
                .build();

        Payment savePayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savePayment);
    }

    @Override
    public PaymentOutputDto findById(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Payment with this id " + id + " not found")
        );
        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentOutputDto> findPayments(Long orderId, String status, Long userId) {

        Status paymentStatus = status != null ? Status.valueOf(status) : null;

        List<Payment> payments;

        if (userId != null && orderId != null && paymentStatus != null) {
            payments = paymentRepository.findByUserIdAndOrderIdAndStatus(userId, orderId, paymentStatus);

        } else if (userId != null) {
            payments = paymentRepository.findByUserId(userId);

        } else {
            payments = paymentRepository.findAll();
        }

        return payments.stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public PaymentSummaryResponse getUserSummary(Long userId, LocalDateTime from, LocalDateTime to) {

        List<Payment> payments =
                paymentRepository.findSuccessfulPaymentsForUser(userId, from, to);

        BigDecimal total = payments.stream()
                .map(Payment::getPaymentAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PaymentSummaryResponse(total);
    }

    @Override
    public PaymentSummaryResponse getGlobalSummary(LocalDateTime from, LocalDateTime to) {

        List<Payment> payments =
                paymentRepository.findSuccessfulPaymentsOverall(from, to);

        BigDecimal total = payments.stream()
                .map(Payment::getPaymentAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PaymentSummaryResponse(total);

    }


}
