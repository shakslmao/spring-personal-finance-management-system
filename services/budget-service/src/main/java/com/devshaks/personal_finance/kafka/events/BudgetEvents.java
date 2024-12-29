package com.devshaks.personal_finance.kafka.events;

public enum BudgetEvents {
    BUDGET_CREATED,
    BUDGET_UPDATED,
    BUDGET_DELETED,
    BUDGET_THRESHOLD_EXCEEDED,
    BUDGET_REJECTED,
    BUDGET_CATEGORY_CREATED,
    TRANSACTION_BUDGET_VALIDATION_FAILED,
}
