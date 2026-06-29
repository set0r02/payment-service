package com.innowise.paymentservice.config.kafka.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaProperties(
        String bootstrapServers
) {

}
