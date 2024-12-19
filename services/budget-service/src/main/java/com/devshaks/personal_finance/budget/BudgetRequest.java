package com.devshaks.personal_finance.budget;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record BudgetRequest(
        @NotNull
        BigDecimal monthlyLimit,

        @NotNull
        YearMonth month,

        List<@Valid BudgetCategoryRequest> categories
 ) {
}
