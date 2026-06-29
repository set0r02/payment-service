package com.innowise.paymentservice;


import com.innowise.paymentservice.config.kafka.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
public class PaymentServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
