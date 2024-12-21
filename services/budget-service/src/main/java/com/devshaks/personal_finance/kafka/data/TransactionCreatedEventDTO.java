package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.kafka.transactions.TransactionsType;

import java.math.BigDecimal;

public record TransactionCreatedEventDTO(
        Long transactionId,
        Long userId,
        String category,
        BigDecimal amount,
        TransactionsType transactionsType,
        String description

) {
}
