package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.Status;
import com.innowise.paymentservice.repository.custom.PaymentRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment,Long>, PaymentRepositoryCustom {

    @Query(value = """
{
  'status': 'SUCCESS',
  'timestamp': { $gte: ?0, $lte: ?1 }
}
""", fields = "{ 'paymentAmount': 1 }")
    List<Payment> findSuccessfulPaymentsOverall(LocalDateTime from, LocalDateTime to);

    @Query(value = """
{
  'userId': ?0,
  'status': 'SUCCESS',
  'timestamp': { $gte: ?1, $lte: ?2 }
}
""", fields = "{ 'paymentAmount': 1 }")
    List<Payment> findSuccessfulPaymentsForUser(Long userId, LocalDateTime from, LocalDateTime to);
}
