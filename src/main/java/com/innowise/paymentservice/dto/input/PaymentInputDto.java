package com.innowise.paymentservice.dto.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentInputDto(

        @NotNull(message = "Order id is required")
        Long orderId,

        @NotNull(message = "paymentAmount must not be null")
        @Positive(message = "Payment amount must be greater than 0")
        BigDecimal paymentAmount


) {

}
