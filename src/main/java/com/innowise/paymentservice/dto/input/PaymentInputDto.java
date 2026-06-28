package com.innowise.paymentservice.dto.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentInputDto(

        @NotNull(message = "Order id is required")
        Long orderId,

        @NotNull(message = "paymentAmount must not be null")
        @DecimalMin(value = "0.01", message = "paymentAmount must be greater  than 0")
        BigDecimal paymentAmount


) {

}
