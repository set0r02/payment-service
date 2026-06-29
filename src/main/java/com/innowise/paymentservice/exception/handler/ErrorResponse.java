package com.innowise.paymentservice.exception.handler;

import java.time.Instant;


public record ErrorResponse(
        int status,
        String message,
        Instant time
) {
}
