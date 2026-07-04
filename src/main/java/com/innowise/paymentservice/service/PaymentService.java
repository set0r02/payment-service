package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.dto.input.PaymentInputDto;
import com.innowise.paymentservice.dto.output.PaymentOutputDto;
import com.innowise.paymentservice.model.Status;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    PaymentOutputDto createPayment(PaymentInputDto paymentInputDto, Long userId);

    PaymentOutputDto findById(String id);
    List<PaymentOutputDto> findPayments(Long orderId, Status status, Authentication authentication);

    PaymentSummaryResponse getUserSummary(Long userId, LocalDateTime from, LocalDateTime to);

    PaymentSummaryResponse getGlobalSummary(LocalDateTime from, LocalDateTime to);
}