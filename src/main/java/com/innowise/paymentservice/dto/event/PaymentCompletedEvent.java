package com.innowise.paymentservice.dto.event;

public record PaymentCompletedEvent(
        Long orderId,
        String status
){
}
