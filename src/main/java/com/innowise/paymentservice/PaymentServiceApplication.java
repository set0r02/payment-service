package com.innowise.paymentservice;


import com.innowise.paymentservice.config.kafka.properties.KafkaProperties;
import com.innowise.paymentservice.config.kafka.properties.KafkaTopicsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties({KafkaProperties.class, KafkaTopicsProperties.class})
@EnableFeignClients
public class PaymentServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
