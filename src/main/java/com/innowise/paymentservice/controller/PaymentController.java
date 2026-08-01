package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.dto.input.PaymentInputDto;
import com.innowise.paymentservice.dto.output.PaymentOutputDto;
import com.innowise.paymentservice.model.Status;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;



import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentOutputDto> createPayment(
            @Valid @RequestBody PaymentInputDto paymentInputDto,
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());
        PaymentOutputDto response = paymentService.createPayment(paymentInputDto, userId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or @paymentSecurity.isOwner(#id, T(Long).valueOf(authentication.principal.subject))")
    public ResponseEntity<PaymentOutputDto> findById(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<PaymentOutputDto>> findPayments(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Status status,
            Authentication authentication
    ) {

        return ResponseEntity.ok(paymentService.findPayments(orderId, status, authentication));
    }

    @GetMapping("/users/{userId}/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or @paymentSecurity.isSelf(#userId, authentication.principal.subject)")
    public ResponseEntity<PaymentSummaryResponse> getUserSummary(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {

        return ResponseEntity.ok(paymentService.getUserSummary(userId, from, to));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaymentSummaryResponse> getGlobalSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return ResponseEntity.ok(paymentService.getGlobalSummary(from, to));

    }
}
