package com.devshaks.personal_finance.kafka.transactions;

public enum TransactionEvents {
    TRANSACTION_CREATED,
    TRANSACTION_UPDATED,
    TRANSACTION_DELETED,
    TRANSACTION_BUDGET_RESTRICTION_APPROVED,
    TRANSACTION_BUDGET_RESTRICTION_REJECTED,
    CATEGORY_ADDED,
    CATEGORY_UPDATED,
}
