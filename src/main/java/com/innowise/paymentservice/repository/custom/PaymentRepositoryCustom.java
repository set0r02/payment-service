package com.innowise.paymentservice.repository.custom;

import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.Status;

import java.util.List;

public interface PaymentRepositoryCustom {
    List<Payment> findByCriteria(Long userId, Long orderId, Status status);
}
