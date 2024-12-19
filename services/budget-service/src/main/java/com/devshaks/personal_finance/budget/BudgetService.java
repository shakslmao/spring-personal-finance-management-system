package com.devshaks.personal_finance.budget;

import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.events.BudgetEvents;
import com.devshaks.personal_finance.user.UserDetailsResponse;
import com.devshaks.personal_finance.user.UserFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.info("Fetching user details for userId: {}", userId);
        UserDetailsResponse userDetails;
        try {
            userDetails =  userFeignClient.getUserProfileDetails(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user details", e);
        }

        if (userDetails == null || userDetails.userId() == null) { throw new RuntimeException("User not found"); }
        Budget budget = budgetMapper.toNewBudget(budgetRequest);
        budget.setUserId(userId);
        Budget savedBudget = budgetRepository.save(budget);
        auditEventSender.sendEventToAudit(BudgetEvents.BUDGET_CREATED, userId, "User Set a New Budget");
        return budgetMapper.mapBudgetToResponse(savedBudget);
    }
}
