package com.devshaks.personal_finance.kafka.consumer;

import com.devshaks.personal_finance.budget.Budget;
import com.devshaks.personal_finance.budget.BudgetRepository;
import com.devshaks.personal_finance.budget.category.BudgetCategory;
import com.devshaks.personal_finance.budget.category.BudgetCategoryRepository;
import com.devshaks.personal_finance.kafka.data.TransactionCreatedEventDTO;
import com.devshaks.personal_finance.kafka.transactions.TransactionEventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaBudgetConsumer {
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final TransactionEventSender transactionEventSender;

    @KafkaListener(topics = "transaction-created", groupId = "budgetGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeBudgetEvents(TransactionCreatedEventDTO transactionEvent) {
        boolean isSuccessful = validateAndUpdateBudget(transactionEvent);
        transactionEventSender.sendEventToTransaction(
                transactionEvent.transactionId(),
                transactionEvent.userId(),
                isSuccessful,
                isSuccessful ? "Transaction Approved" : "Exceeds Budget or Category Limit");
    }

    private boolean validateAndUpdateBudget(TransactionCreatedEventDTO transactionEvent) {
        Budget budget = budgetRepository.findByUserIdAndMonth(transactionEvent.userId(), YearMonth.now().toString())
                .orElseThrow(() -> new RuntimeException("Could not find budget"));

        // Check if transaction exceeds monthly limit.
        if (budget.getRemainingAmount().compareTo(transactionEvent.amount()) < 0) {
            return false;
        }

        // check category limit if category exists
        if (transactionEvent.category() != null) {
            BudgetCategory category = budgetCategoryRepository.findByBudgetAndCategoryName(budget, transactionEvent.category())
                    .orElseThrow(() -> new RuntimeException("Could not find budget category"));

            if (category.getCategoryLimit() != null &&
                    category.getCategoryLimit().subtract(category.getSpentAmount()).compareTo(transactionEvent.amount()) < 0) {
                return false;
            }

            category.setSpentAmount(category.getSpentAmount().add(transactionEvent.amount()));
            budgetCategoryRepository.save(category);
        }

        budget.setSpentAmount(budget.getSpentAmount().add(transactionEvent.amount()));
        budget.setRemainingAmount(budget.getMonthlyLimit().subtract(budget.getSpentAmount()));
        budgetRepository.save(budget);
        return true;
    }
}
