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

/**
 * Service class for managing user budgets.
 * Handles CRUD operations and interactions with Kafka for audit events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository; // Repository for budget persistence.
    private final BudgetMapper budgetMapper; // Mapper for converting between entities and DTOs.
    private final UserFeignClient userFeignClient; // Feign client to communicate with the User Service.
    private final AuditEventSender auditEventSender; // Sends audit events to Kafka.

    /**
     * Creates a new budget for a user.
     * Validates the user's existence and ensures no duplicate budgets exist for the
     * specified month.
     */
    @Transactional
    public BudgetResponse createUserBudget(Long userId, BudgetRequest budgetRequest) {
        // Fetch user details via Feign client.
        UserDetailsResponse userDetails;
        try {
            userDetails = userFeignClient.getUserProfileDetails(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user details", e);
        }

        // Validate user existence.
        if (userDetails == null || userDetails.userId() == null) {
            throw new UserNotFoundException("User not found");
        }

        // Ensure no existing budget for the same month.
        String month = budgetRequest.month().toString();
        budgetRepository.findByUserIdAndMonth(userId, month).ifPresent(existingBudget -> {
            throw new RuntimeException("Budget already exists for this Month");
        });

        // Map the request to a new budget entity and save it.
        Budget budget = budgetMapper.toNewBudget(budgetRequest);
        budget.setUserId(userId);
        Budget savedBudget = budgetRepository.save(budget);

        // Send an audit event.
        try {
            auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_CREATED, userId, "User Set a New Budget");
        } catch (Exception e) {
            throw new AuditEventException("Failed to send Event to Audit.");
        }

        return budgetMapper.mapBudgetToResponse(savedBudget);
    }

    /**
     * Retrieves a budget by its ID.
     * Throws an exception if the budget does not exist.
     */
    public BudgetResponse getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .map(budgetMapper::mapBudgetToResponse)
                .orElseThrow(() -> new BudgetNotFoundException("Budget Was Not Found"));
    }

    /**
     * Retrieves all budgets associated with a user.
     * Throws an exception if no budgets are found.
     */
    public List<BudgetResponse> getUserBudgets(Long userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        if (budgets.isEmpty()) {
            throw new BudgetNotFoundException("Budget Was Not Found");
        }
        return budgets.stream()
                .map(budgetMapper::mapBudgetToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a budget for a user by its ID.
     * Sends an audit event upon successful deletion.
     */
    public void deleteBudget(Long id, Long userId) {
        // Verify if the budget exists.
        if (!budgetRepository.existsByIdAndUserId(id, userId)) {
            throw new BudgetNotFoundException("Budget Was Not Found");
        }

        // Attempt to delete the budget and log errors if they occur.
        try {
            budgetRepository.deleteByIdAndUserId(id, userId);
            auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_DELETED, userId, "User Deleted Budget");
        } catch (Exception e) {
            log.error("Failed to delete budget with ID: {} for user ID: {}. Error: {}", id, userId, e.getMessage());
            throw new AuditEventException("Failed to delete budget");
        }
    }

    /**
     * Updates a user's budget with specific fields.
     * Handles dynamic updates such as monthly limits, categories, or month changes.
     */
    public BudgetResponse updateBudget(Long userId, Long id, Map<String, Object> updates) {
        // Fetch the budget to update or throw an exception if not found.
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget Was Not Found"));

        // Process updates based on the provided fields.
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
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> categoryUpdates = (List<Map<String, Object>>) value;

                    // Handle category updates or additions.
                    List<BudgetCategory> existingCategories = budget.getCategories();
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

                        // Check if the category already exists.
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
                    }).toList();

                    // Update the existing categories list.
                    existingCategories.clear();
                    existingCategories.addAll(updatedCategories);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown field: " + key);
            }
        });

        // Save the updated budget and send an audit event.
        Budget updatedBudget = budgetRepository.save(budget);
        auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_UPDATED, userId, "User Updated Their Budget");
        return budgetMapper.mapBudgetToResponse(updatedBudget);
    }

    /**
     * Adds a new category to an existing budget.
     * Validates category uniqueness before addition.
     */
    public BudgetCategoryResponse addCategoryToBudget(Long userId, Long id, @Valid BudgetCategoryRequest request) {
        // Find the budget associated with the user.
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget Was Not Found"));

        // Check for duplicate category names.
        boolean categoryExists = budget.getCategories()
                .stream()
                .anyMatch(category -> category.getCategoryName().equalsIgnoreCase(request.name()));

        if (categoryExists) {
            throw new BudgetUpdateException("Category Name already exists");
        }

        // Map and save the new category to the budget.
        BudgetCategory category = budgetMapper.toNewCategory(request, budget);
        budget.getCategories().add(category);
        budgetRepository.save(budget);

        // Send an audit event for the new category addition.
        auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_CATEGORY_CREATED, userId, "User Added a New Category");
        return budgetMapper.toNewCategoryResponse(category);
    }
}
