package com.devshaks.personal_finance.payments;

import org.springframework.stereotype.Service;

@Service
public class PaymentMapper {
    public PaymentDTO toPaymentDTO(Payment payment) {
        if (payment == null) { throw new IllegalArgumentException("Payment cannot be null"); }
        return new PaymentDTO(
                payment.getId(),
                payment.getPaymentStripeId(),
                payment.getUserId(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getGatewayResponse()
        );
    }
}
