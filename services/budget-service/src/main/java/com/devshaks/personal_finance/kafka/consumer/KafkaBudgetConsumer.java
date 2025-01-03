package com.devshaks.personal_finance.kafka.consumer;

// Import statements for necessary dependencies and classes.
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

    /**
     * Kafka listener method to consume events from the "transaction-created" topic.
     * Processes the transaction event and validates it against the user's budget.
     *
     * @param transactionEvent The transaction event received from Kafka.
     */
    @KafkaListener(topics = "transaction-created", groupId = "budgetGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeBudgetEvents(TransactionCreatedEventDTO transactionEvent) {
        try {
            // Validate the budget and update if successful
            boolean isSuccessful = validateAndUpdateBudget(transactionEvent);

            // Notify transaction service of successful validation
            transactionEventSender.sendEventToTransaction(
                    transactionEvent.transactionId(),
                    transactionEvent.userId(),
                    isSuccessful,
                    "Transaction approved and budget updated");
        } catch (BudgetExceededException | BudgetNotFoundException | BudgetCategoryNotFoundException ex) {
            // Handle specific budget validation errors
            log.error("Validation failed for transaction {}: {}", transactionEvent.transactionId(), ex.getMessage());
            transactionEventSender.sendEventToTransaction(
                    transactionEvent.transactionId(),
                    transactionEvent.userId(),
                    false,
                    ex.getMessage());

            // Send audit event for failed validation
            auditEventSender.sendEventToAudit(
                    TRANSACTION_BUDGET_VALIDATION_FAILED,
                    transactionEvent.userId(),
                    "Transaction Failed Budget Validation: " + ex.getMessage());

        } catch (Exception ex) {
            // Handle unexpected errors
            log.error("Unexpected error occurred for transaction {}: {}", transactionEvent.transactionId(),
                    ex.getMessage());
            transactionEventSender.sendEventToTransaction(
                    transactionEvent.transactionId(),
                    transactionEvent.userId(),
                    false,
                    "Unexpected error occurred: " + ex.getMessage());
        }
    }

    /**
     * Validates the transaction against the user's budget and updates the budget
     * records.
     *
     * @param transactionEvent The transaction event containing transaction details.
     * @return true if the transaction is valid and budget updated successfully.
     */
    private boolean validateAndUpdateBudget(TransactionCreatedEventDTO transactionEvent) {
        // Retrieve the user's current monthly budget
        Budget budget = budgetRepository.findByUserIdAndMonth(transactionEvent.userId(), YearMonth.now().toString())
                .orElseThrow(() -> new BudgetNotFoundException("Could not find budget"));

        // Check if the transaction exceeds the overall monthly budget limit
        if (budget.getRemainingAmount().compareTo(transactionEvent.amount()) < 0) {
            throw new BudgetExceededException("Transaction Exceeds Monthly Budget Limit");
        }

        // Check if the transaction exceeds the category limit (if a category is
        // specified)
        if (transactionEvent.category() != null) {
            BudgetCategory category = budgetCategoryRepository
                    .findByBudgetAndCategoryName(budget, transactionEvent.category())
                    .orElse(null);

            // Validate category limits if the category exists
            if (category != null) {
                if (category.getCategoryLimit() != null &&
                        category.getCategoryLimit().subtract(category.getSpentAmount())
                                .compareTo(transactionEvent.amount()) < 0) {
                    throw new BudgetExceededException("Transaction Exceeds Category Limit");
                }

                // Update the category's spent amount
                category.setSpentAmount(category.getSpentAmount().add(transactionEvent.amount()));
                budgetCategoryRepository.save(category);
            }
        }

        // Update the budget's spent and remaining amounts
        budget.setSpentAmount(budget.getSpentAmount().add(transactionEvent.amount()));
        budget.setRemainingAmount(budget.getMonthlyLimit().subtract(budget.getSpentAmount()));
        budgetRepository.save(budget);

        return true; // Validation and update successful
    }
}
