package com.devshaks.personal_finance.payments;

import java.math.BigDecimal;

public record PaymentRequest(
        Long transactionId,
        Long userId,
        BigDecimal amount,
        String currency,
        String paymentStripeId

) {
}
