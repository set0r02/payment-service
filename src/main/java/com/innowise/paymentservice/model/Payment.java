package com.innowise.paymentservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "payments")
public class Payment {

    @Id
    @Field(name = "id")
    private String id;

    @Field(name = "order_id")
    private Long orderId;

    @Field(name = "user_id")
    private Long userId;

    @Field(name = "status")
    private Status status;

    @Field(name = "timestamp")
    private LocalDateTime timestamp;

    @Field(name = "payment_amount")
    private BigDecimal paymentAmount;

}
