    package com.innowise.paymentservice.controller;

    import com.innowise.paymentservice.dto.PaymentSummaryResponse;
    import com.innowise.paymentservice.dto.input.PaymentInputDto;
    import com.innowise.paymentservice.dto.output.PaymentOutputDto;
    import com.innowise.paymentservice.service.PaymentService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
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
        @PreAuthorize("hasAnyRole('USER','ADMIN')")
        public ResponseEntity<PaymentOutputDto> createPayment(
                @Valid @RequestBody PaymentInputDto paymentInputDto,
                @AuthenticationPrincipal Jwt jwt){

            Long userId = Long.valueOf(jwt.getSubject());
            PaymentOutputDto response = paymentService.createPayment(paymentInputDto, userId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN') or @paymentSecurity.isOwner(#id, authentication.principal.subject)")
        public ResponseEntity<PaymentOutputDto> findById(@PathVariable Long id){
            return ResponseEntity.ok(paymentService.findById(id));
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('USER','ADMIN')")
        public ResponseEntity<List<PaymentOutputDto>> findPayments(
                @RequestParam(required = false) Long orderId,
                @RequestParam(required = false) String status,
                @RequestParam(required = false) Long userId,
                @AuthenticationPrincipal Jwt jwt
        ){

            Long currentUserId = Long.valueOf(jwt.getSubject());

            Long effectiveUserId = (userId != null)
                    ? userId
                    : currentUserId;

            return ResponseEntity.ok(paymentService.findPayments(orderId, status, effectiveUserId)
            );
        }

        @GetMapping("/users/{userId}/summary")
        @PreAuthorize("hasRole('ADMIN') or @paymentSecurity.isOwnerUser(#userId, authentication.principal.subject)")
        public ResponseEntity<PaymentSummaryResponse> getUserSummary(
                @PathVariable Long userId,
                @RequestParam LocalDateTime from,
                @RequestParam LocalDateTime to
                ){

            return ResponseEntity.ok(
                    paymentService.getUserSummary(userId, from, to)
            );
        }

        @GetMapping("/summary")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<PaymentSummaryResponse> getGlobalSummary(
                @RequestParam LocalDateTime from,
                @RequestParam LocalDateTime to){
            return ResponseEntity.ok(paymentService.getGlobalSummary(from,to));

        }


    }
