package com.devshaks.personal_finance.transactions;

import com.devshaks.personal_finance.users.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionsResponse(
        Long userId,
        Long transactionId,
        String category,
        BigDecimal amount,
        LocalDateTime transactionDate,
        String eventDescription,
        String transactionDescription
) {
}
