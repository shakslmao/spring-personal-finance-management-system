package com.devshaks.personal_finance.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionsResponse(
        Long userId,
        Long transactionId,
        String category,
        BigDecimal amount,
        LocalDateTime transactionDate,
        TransactionsType transactionType,
        TransactionsStatus transactionStatus,
        String description
) {
}
