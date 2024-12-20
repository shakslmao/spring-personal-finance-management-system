package com.devshaks.personal_finance.budget;

import com.devshaks.personal_finance.exceptions.AuditEventException;
import com.devshaks.personal_finance.exceptions.BudgetNotFoundException;
import com.devshaks.personal_finance.exceptions.UserNotFoundException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.events.BudgetEvents;
import com.devshaks.personal_finance.user.UserDetailsResponse;
import com.devshaks.personal_finance.user.UserFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }
}
