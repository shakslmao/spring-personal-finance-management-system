package com.devshaks.personal_finance.payments;

public record PaymentResponse(
        String paymentStripeId,
        PaymentStatus status
) {
}
