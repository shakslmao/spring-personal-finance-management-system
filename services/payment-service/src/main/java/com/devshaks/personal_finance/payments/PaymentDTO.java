package com.devshaks.personal_finance.payments;

import java.math.BigDecimal;

public record PaymentDTO(
        Long id,
        String paymentStripeId,
        Long userId,
        Long transactionId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String gatewayResponse
) {
}
