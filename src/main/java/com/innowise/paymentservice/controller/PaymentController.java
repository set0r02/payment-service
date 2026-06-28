package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.input.PaymentInputDto;
import com.innowise.paymentservice.dto.output.PaymentOutputDto;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentOutputDto> createPayment(@Valid @RequestBody PaymentInputDto paymentInputDto, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());
        PaymentOutputDto response = paymentService.createPayment(paymentInputDto, userId);
        return ResponseEntity.accepted().body(response);
    }
}
