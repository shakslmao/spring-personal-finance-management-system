package com.devshaks.personal_finance.payments;

import com.devshaks.personal_finance.transactions.PaymentStatus;

public record PaymentResponse(
        String paymentStripeId,
        PaymentStatus status,
        String gatewayResponse) {

}
