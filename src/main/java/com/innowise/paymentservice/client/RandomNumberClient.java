package com.innowise.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "randomNumberClient", url = "${app.random-number")
public interface RandomNumberClient {

    @GetMapping
    String getRandomNumber();
}
