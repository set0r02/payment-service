package com.innowise.paymentservice.repository.custom.impl;

import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.Status;
import com.innowise.paymentservice.repository.custom.PaymentRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.core.query.Query;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Payment> findPayments(Long userId, Long orderId, Status status) {

        Query query = new Query();
        if (userId != null) {
            query.addCriteria(Criteria.where("user_Id").is(userId));
        }

        if (orderId != null) {
            query.addCriteria(Criteria.where("order_Id").is(orderId));
        }

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        return mongoTemplate.find(query, Payment.class);
    }
}
