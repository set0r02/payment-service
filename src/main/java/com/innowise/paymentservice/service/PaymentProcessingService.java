package com.innowise.paymentservice.service;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.config.kafka.properties.KafkaTopicsProperties;
import com.innowise.paymentservice.dto.event.PaymentCompletedEvent;
import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.Status;
import com.innowise.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;
    private final RandomNumberClient randomNumberClient;
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Async
    public void processPayment(String id){
        Payment payment = paymentRepository.findById(id)
                .orElseThrow();

        Status newStatus = determineStatus();

        payment.setStatus(newStatus);
        payment.setTimestamp(LocalDateTime.now());

        Payment updated = paymentRepository.save(payment);

        kafkaTemplate.send(kafkaTopicsProperties.paymentEvents(),
                            new PaymentCompletedEvent(
                                    updated.getOrderId(),
                                    updated.getStatus().name()
                            )
        );
    }

    private Status determineStatus(){
        int number = Integer.parseInt(randomNumberClient.getRandomNumber().trim());
        return number % 2 == 0 ? Status.SUCCESS : Status.FAILED;
    }

}
