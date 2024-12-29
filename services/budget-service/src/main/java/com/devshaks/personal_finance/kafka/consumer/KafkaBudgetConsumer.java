package com.devshaks.personal_finance.kafka.consumer;

import com.devshaks.personal_finance.budget.Budget;
import com.devshaks.personal_finance.budget.BudgetRepository;
import com.devshaks.personal_finance.budget.category.BudgetCategory;
import com.devshaks.personal_finance.budget.category.BudgetCategoryRepository;
import com.devshaks.personal_finance.exceptions.BudgetCategoryNotFoundException;
import com.devshaks.personal_finance.exceptions.BudgetExceededException;
import com.devshaks.personal_finance.exceptions.BudgetNotFoundException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.data.TransactionCreatedEventDTO;
import com.devshaks.personal_finance.kafka.transactions.TransactionEventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

import static com.devshaks.personal_finance.kafka.events.BudgetEvents.TRANSACTION_BUDGET_VALIDATION_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaBudgetConsumer {
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final TransactionEventSender transactionEventSender;
    private final AuditEventSender auditEventSender;

    @KafkaListener(topics = "transaction-created", groupId = "budgetGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeBudgetEvents(TransactionCreatedEventDTO transactionEvent) {
        try {
            boolean isSuccessful = validateAndUpdateBudget(transactionEvent);
            transactionEventSender.sendEventToTransaction(
                    transactionEvent.transactionId(),
                    transactionEvent.userId(),
                    isSuccessful,
                    "Transaction approved and budget updated");
        } catch (BudgetExceededException | BudgetNotFoundException | BudgetCategoryNotFoundException ex) {
            log.error("Validation failed for transaction {}: {}", transactionEvent.transactionId(), ex.getMessage());
            transactionEventSender.sendEventToTransaction(
                    transactionEvent.transactionId(),
                    transactionEvent.userId(),
                    false,
                    ex.getMessage());
            auditEventSender.sendEventToAudit(
                    TRANSACTION_BUDGET_VALIDATION_FAILED,
                    transactionEvent.userId(),
                    "Transaction Failed Budget Validation: " + ex.getMessage());

        } catch (Exception ex) {
            log.error("Unexpected error occurred for transaction {}: {}", transactionEvent.transactionId(), ex.getMessage());
            transactionEventSender.sendEventToTransaction(
                    transactionEvent.transactionId(),
                    transactionEvent.userId(),
                    false,
                    "Unexpected error occurred" + ex.getMessage());
        }
    }

    private boolean validateAndUpdateBudget(TransactionCreatedEventDTO transactionEvent) {
        Budget budget = budgetRepository.findByUserIdAndMonth(transactionEvent.userId(), YearMonth.now().toString())
                .orElseThrow(() -> new BudgetNotFoundException("Could not find budget"));

        // Check if transaction exceeds monthly limit.
        if (budget.getRemainingAmount().compareTo(transactionEvent.amount()) < 0) {
            throw new BudgetExceededException("Transaction Exceeds Monthly Budget Limit");
        }

        // check category limit if category exists
        if (transactionEvent.category() != null) {
            BudgetCategory category = budgetCategoryRepository.findByBudgetAndCategoryName(budget, transactionEvent.category())
                    .orElse(null);

            if (category != null) {
                if (category.getCategoryLimit() != null &&
                        category.getCategoryLimit().subtract(category.getSpentAmount()).compareTo(transactionEvent.amount()) < 0) {
                    throw new BudgetExceededException("Transaction Exceeds Monthly Budget Limit");
                }

                // Update category spent amount
                category.setSpentAmount(category.getSpentAmount().add(transactionEvent.amount()));
                budgetCategoryRepository.save(category);
            }
        }

        // Update budget spent and remaining amounts
        budget.setSpentAmount(budget.getSpentAmount().add(transactionEvent.amount()));
        budget.setRemainingAmount(budget.getMonthlyLimit().subtract(budget.getSpentAmount()));
        budgetRepository.save(budget);
        return true;
    }
}
