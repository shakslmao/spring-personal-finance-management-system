package com.devshaks.personal_finance.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionsDTO(
        Long id,
        Long userId,
        String category,
        BigDecimal amount,
        LocalDateTime transactionDate,
        TransactionsType transactionType,
        TransactionsStatus transactionStatus,
        String description,
        List<String> tags) {
}
