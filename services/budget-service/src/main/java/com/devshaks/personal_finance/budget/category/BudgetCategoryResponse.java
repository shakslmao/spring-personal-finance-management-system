package com.devshaks.personal_finance.budget.category;

import java.math.BigDecimal;

public record BudgetCategoryResponse(
        Long id,
        String name,
        BigDecimal categoryLimit,
        BigDecimal spentAmount
) {
}
