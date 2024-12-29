package com.devshaks.personal_finance.kafka.data;

public record TransactionValidatedEventDTO (
        Long transactionId,
        Long userId,
        Boolean isSuccessful,
        String reason
) {
}
