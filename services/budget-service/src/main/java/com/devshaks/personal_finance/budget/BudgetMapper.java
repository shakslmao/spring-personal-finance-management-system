package com.devshaks.personal_finance.budget;

import com.devshaks.personal_finance.budget.category.BudgetCategory;
import com.devshaks.personal_finance.budget.category.BudgetCategoryRequest;
import com.devshaks.personal_finance.budget.category.BudgetCategoryResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetMapper {
    public Budget toNewBudget(BudgetRequest budgetRequest) {
        Budget budget = Budget.builder()
                .monthlyLimit(budgetRequest.monthlyLimit())
                .spentAmount(BigDecimal.ZERO)
                .remainingAmount(budgetRequest.monthlyLimit())
                .month(budgetRequest.month().toString())
                .build();

        if (budgetRequest.categories() != null && !budgetRequest.categories().isEmpty()) {
            List<BudgetCategory> categories = budgetRequest.categories()
                    .stream()
                    .map(categoryRequest -> toNewCategory(categoryRequest, budget))
                    .collect(Collectors.toList());
            budget.setCategories(categories);
        } else {
            budget.setCategories(Collections.emptyList());
        }

        return budget;
    }

    private BudgetCategory toNewCategory(BudgetCategoryRequest categoryRequest, Budget budget) {
        return BudgetCategory.builder()
                .categoryName(categoryRequest.name())
                .categoryLimit(categoryRequest.categoryLimit() != null ? categoryRequest.categoryLimit() : BigDecimal.ZERO)
                .spentAmount(BigDecimal.ZERO)
                .budget(budget)
                .build();
    }

    public BudgetResponse mapBudgetToResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getUserId(),
                budget.getMonthlyLimit(),
                budget.getSpentAmount(),
                budget.getRemainingAmount(),
                YearMonth.parse(budget.getMonth()),
                budget.getCategories()
                        .stream()
                        .map(categories -> new BudgetCategoryResponse(
                                categories.getId(),
                                categories.getCategoryName(),
                                categories.getCategoryLimit(),
                                categories.getSpentAmount()))
                        .collect(Collectors.toList())

        );
    }
}



/*
{
    "monthlyLimit": 5000.00,
    "month": "2024-12",
    "categories": [
        {
            "name": "Groceries",
            "categoryLimit": 1500.00
        },
        {
            "name": "Entertainment",
            "categoryLimit": 1000.00
        }
    ]
}

{
    "monthlyLimit": 5000.00,
    "month": "2024-12",
    "categories": [
        {
            "name": "Groceries",
            "categoryLimit": 1500.00
        }
    ]
}

{
    "monthlyLimit": 5000.00,
    "month": "2024-12"
}







 */