package com.devshaks.personal_finance.budget;

import com.devshaks.personal_finance.budget.category.BudgetCategoryResponse;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record BudgetResponse(
        Long id,
        Long userId,
        BigDecimal monthlyLimit,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        YearMonth month,
        List<BudgetCategoryResponse> categories
) {
}
