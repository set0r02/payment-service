    package com.innowise.paymentservice.security;

    import com.innowise.paymentservice.repository.PaymentRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Component;

    @Component("paymentSecurity")
    @RequiredArgsConstructor
    public class PaymentSecurity {

        private final PaymentRepository paymentRepository;

        public boolean isOwner(Long paymentId, Long userId) {
            return paymentRepository.findById(paymentId)
                    .map(p -> p.getUserId().equals(userId))
                    .orElse(false);
        }
    }
