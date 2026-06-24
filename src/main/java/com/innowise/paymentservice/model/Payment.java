package com.innowise.paymentservice.model;

import jakarta.persistence.Entity;
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

    private Long id;

    private Long order_id;

    private Long user_id;

    private Status status;

    private LocalDateTime timestamp;

    private BigDecimal payment_amount;

}
