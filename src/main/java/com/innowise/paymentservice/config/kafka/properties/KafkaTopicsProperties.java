package com.innowise.paymentservice.config.kafka.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
        String paymentEvents
) {
}
