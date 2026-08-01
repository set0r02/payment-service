package com.innowise.paymentservice.repository.custom.impl;

import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.Status;
import com.innowise.paymentservice.repository.custom.PaymentRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Payment> findByCriteria(Long userId, Long orderId, Status status) {

        Query query = new Query();

        if (userId != null) {
            query.addCriteria(Criteria.where("userId").is(userId));
        }

        if (orderId != null) {
            query.addCriteria(Criteria.where("orderId").is(orderId));
        }

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        return mongoTemplate.find(query, Payment.class);
    }
}
