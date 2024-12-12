package com.devshaks.personal_finance.transactions;

import org.w3c.dom.stylesheets.LinkStyle;

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
