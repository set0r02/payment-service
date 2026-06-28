package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.dto.input.PaymentInputDto;
import com.innowise.paymentservice.dto.output.PaymentOutputDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    PaymentOutputDto createPayment(PaymentInputDto paymentInputDto, Long userId);

    PaymentOutputDto findById(Long id);

    List<PaymentOutputDto> findPayments(Long orderId, String status, Long userId);

    PaymentSummaryResponse getUserSummary(Long userId, LocalDateTime from, LocalDateTime to);

    PaymentSummaryResponse getGlobalSummary(LocalDateTime from, LocalDateTime to);
}