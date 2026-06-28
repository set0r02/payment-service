package com.innowise.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentSummaryResponse(
        BigDecimal total
) {
}
