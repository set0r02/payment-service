package com.innowise.paymentservice.client.service;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.model.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceGateway {

    private final RandomNumberClient randomNumberClient;

    public Status processPayment() {

        int number = Integer.parseInt(randomNumberClient.getRandomNumber().trim());

        return (number % 2 == 0)
                ? Status.SUCCESS
                : Status.FAILED;
    }

}
