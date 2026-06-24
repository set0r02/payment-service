package com.innowise.paymentservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "order_id",nullable = false)
    private Long order_id;

    @Column(name = "user_id",nullable = false)
    private Long user_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private Status status;

    @Column(name = "timestamp",nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "payment_amount",nullable = false)
    private BigDecimal paymentAmount;

}
