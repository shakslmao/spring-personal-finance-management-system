package com.devshaks.personal_finance.payments;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentStripeId(String paymentStripeId);

    Optional<Payment> findByTransactionId(Long transactionId);

    Optional<Payment> findByTransactionIdAndUserId(Long transactionId, Long userId);
}
