package com.devshaks.personal_finance.budget;

import java.math.BigDecimal;

public record BudgetCategoryResponse(
        Long id,
        String name,
        BigDecimal categoryLimit,
        BigDecimal spentAmount
) {
}
