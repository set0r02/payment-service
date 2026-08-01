package com.innowise.paymentservice.dto.output;

import com.innowise.paymentservice.model.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentOutputDto(

        String id,

        Long orderId,

        Long userId,

        Status status,

        LocalDateTime timestamp,

        BigDecimal paymentAmount
) {
}
