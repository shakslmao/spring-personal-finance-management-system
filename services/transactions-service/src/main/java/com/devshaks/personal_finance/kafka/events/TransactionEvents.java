package com.devshaks.personal_finance.kafka.events;

public enum TransactionEvents {
    TRANSACTION_CREATED,
    TRANSACTION_UPDATED,
    TRANSACTION_DELETED,
    TRANSACTION_FAILED_BUDGET_EXCEEDED,
    TRANSACTION_BUDGET_RESTRICTION_APPROVED,
    TRANSACTION_BUDGET_RESTRICTION_REJECTED,
    CATEGORY_ADDED,
    CATEGORY_UPDATED,
    TRANSACTION_FAILED_PAYMENT_REJECTED,
    TRANSACTION_SUCCESS_PAYMENT_COMPLETED,
}
