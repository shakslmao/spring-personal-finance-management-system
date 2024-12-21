package com.devshaks.personal_finance.budget;

import com.devshaks.personal_finance.budget.category.BudgetCategory;
import com.devshaks.personal_finance.budget.category.BudgetCategoryRequest;
import com.devshaks.personal_finance.budget.category.BudgetCategoryResponse;
import com.devshaks.personal_finance.exceptions.AuditEventException;
import com.devshaks.personal_finance.exceptions.BudgetNotFoundException;
import com.devshaks.personal_finance.exceptions.BudgetUpdateException;
import com.devshaks.personal_finance.exceptions.UserNotFoundException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.events.BudgetEvents;
import com.devshaks.personal_finance.user.UserDetailsResponse;
import com.devshaks.personal_finance.user.UserFeignClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final UserFeignClient userFeignClient;
    private final AuditEventSender auditEventSender;

    @Transactional
    public BudgetResponse createUserBudget(Long userId, BudgetRequest budgetRequest) {
        UserDetailsResponse userDetails;
        try {
            userDetails =  userFeignClient.getUserProfileDetails(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user details", e);
        }

        if (userDetails == null || userDetails.userId() == null) {
            throw new UserNotFoundException("User not found");
        }
        String month = budgetRequest.month().toString();
        budgetRepository.findByUserIdAndMonth(userId, month).ifPresent(existingBudget -> {
            throw new RuntimeException("Budget already exists for this Month");
        });

        Budget budget = budgetMapper.toNewBudget(budgetRequest);
        budget.setUserId(userId);
        Budget savedBudget = budgetRepository.save(budget);
        try {
            auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_CREATED, userId, "User Set a New Budget");

        } catch (Exception e) {
            throw new AuditEventException("Failed to send Event to Audit.");
        }
        return budgetMapper.mapBudgetToResponse(savedBudget);
    }

    public BudgetResponse getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .map(budgetMapper::mapBudgetToResponse)
                .orElseThrow(() -> new BudgetNotFoundException("Budget Was Not Found"));
    }

    public List<BudgetResponse> getUserBudgets(Long userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        if (budgets.isEmpty()) { throw new BudgetNotFoundException("Budget Was Not Found"); }
        return budgets.stream()
                .map(budgetMapper::mapBudgetToResponse)
                .collect(Collectors.toList());
    }

    public void deleteBudget(Long id, Long userId) {
        if (!budgetRepository.existsByIdAndUserId(id, userId)) {
            throw new BudgetNotFoundException("Budget Was Not Found");
        }
        try {
            budgetRepository.deleteByIdAndUserId(id, userId);
            auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_DELETED, userId, "User Deleted Budget");

        } catch (Exception e) {
            log.error("Failed to delete budget with ID: {} for user ID: {}. Error: {}", id, userId, e.getMessage());
            throw new AuditEventException("Failed to delete budget");
        }
    }

    public BudgetResponse updateBudget(Long userId, Long id, Map<String, Object> updates) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget Was Not Found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "monthlyLimit":
                    BigDecimal newMonthlyLimit = new BigDecimal(value.toString());
                    budget.setMonthlyLimit(newMonthlyLimit);
                    budget.setRemainingAmount(newMonthlyLimit.subtract(budget.getSpentAmount()));
                    break;
                case "month":
                    budget.setMonth(String.valueOf(YearMonth.parse(value.toString())));
                    break;
                case "categories":
                List<Map<String, Object>> categoryUpdates = (List<Map<String, Object>>) value;

                // Get existing categories
                List<BudgetCategory> existingCategories = budget.getCategories();

                // map new categories
                List<BudgetCategory> updatedCategories = categoryUpdates.stream().map(categoryRequest -> {
                    String name = categoryRequest.get("name") != null
                            ? categoryRequest.get("name").toString()
                            : null;
                    BigDecimal categoryLimit = categoryRequest.get("categoryLimit") != null
                            ? new BigDecimal(categoryRequest.get("categoryLimit").toString())
                            : null;

                    if (name == null || categoryLimit == null) {
                        throw new BudgetUpdateException("Category Name and Category Limit cannot be null");
                    }

                    // check if the category exists
                    return existingCategories.stream()
                            .filter(c -> c.getCategoryName().equals(name))
                            .findFirst()
                            .map(existingCategory -> {
                                existingCategory.setCategoryLimit(categoryLimit);
                                return existingCategory;
                            })
                            .orElseGet(() -> {
                                BudgetCategory newCategory = new BudgetCategory();
                                newCategory.setCategoryLimit(categoryLimit);
                                newCategory.setBudget(budget);
                                return newCategory;
                            });

                }).collect(Collectors.toList());

                // set the updated categories
                existingCategories.clear();
                existingCategories.addAll(updatedCategories);
                break;
                default:
                    throw new IllegalArgumentException("Unknown field: " + key);
            }
        });

        Budget updatedBudget = budgetRepository.save(budget);
        auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_UPDATED, userId, "User Updated Their Budget");
        return budgetMapper.mapBudgetToResponse(updatedBudget);
    }

    public BudgetCategoryResponse addCategoryToBudget(Long userId, Long id, @Valid BudgetCategoryRequest request) {
        // find budget by id.
        Budget budget = budgetRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new BudgetNotFoundException("Budget Was Not Found"));

        // validate that the category name doest not already exist.
        boolean categoryExists = budget.getCategories()
                .stream()
                .anyMatch(category -> category.getCategoryName().equalsIgnoreCase(request.name()));

        if (categoryExists) { throw new BudgetUpdateException("Category Name already exists"); }

        // create the new category
        BudgetCategory category = budgetMapper.toNewCategory(request, budget);
        budget.getCategories().add(category);
        budgetRepository.save(budget);
        auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_CATEGORY_CREATED, userId, "User Added a New Category");
        return budgetMapper.toNewCategoryResponse(category);
    }
}
