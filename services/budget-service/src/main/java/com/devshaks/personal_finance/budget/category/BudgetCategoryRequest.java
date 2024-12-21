package com.devshaks.personal_finance.budget.category;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetCategoryRequest(
        @NotNull
        String name,

        @NotNull
        BigDecimal categoryLimit
) {
}
