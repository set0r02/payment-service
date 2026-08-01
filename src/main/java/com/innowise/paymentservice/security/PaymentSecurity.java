    package com.innowise.paymentservice.security;

    import com.innowise.paymentservice.repository.PaymentRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Component;

    @Component("paymentSecurity")
    @RequiredArgsConstructor
    public class PaymentSecurity {

        private final PaymentRepository paymentRepository;

        public boolean isOwner(String paymentId, Long userId) {
            return paymentRepository.findById(paymentId)
                    .map(p -> p.getUserId().equals(userId))
                    .orElse(false);
        }

        public boolean isSelf(Long userId, String subject) {
            return userId.equals(Long.valueOf(subject));
        }
    }
