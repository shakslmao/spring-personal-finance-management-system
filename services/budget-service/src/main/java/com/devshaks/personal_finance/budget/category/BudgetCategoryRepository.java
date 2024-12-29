package com.devshaks.personal_finance.budget.category;

import com.devshaks.personal_finance.budget.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {
    Optional <BudgetCategory> findByBudgetAndCategoryName(Budget budget, String categoryName);
}
