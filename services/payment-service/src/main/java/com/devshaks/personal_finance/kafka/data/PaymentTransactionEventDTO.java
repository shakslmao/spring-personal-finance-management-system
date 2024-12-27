package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.kafka.transactions.PaymentStatus;

import java.math.BigDecimal;


public record PaymentTransactionEventDTO(
        Long transactionId,
        Long userId,
        BigDecimal amount,
        PaymentStatus status
) {
}
