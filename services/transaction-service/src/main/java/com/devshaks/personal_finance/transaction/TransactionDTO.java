package com.devshaks.personal_finance.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionDTO(
        Long id,
        Long userId,
        String category,
        BigDecimal amount,
        LocalDateTime transactionDate,
        TransactionType transactionType,
        TransactionStatus transactionStatus,
        String description,
        List<String> tags

) {
}
