package com.devshaks.personal_finance.kafka.transaction;

import com.devshaks.personal_finance.kafka.data.TransactionCreatedEventDTO;
import com.devshaks.personal_finance.transactions.TransactionsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionEventSender {
    private final TransactionEventProducer transactionEventProducer;

    public void sendEventToBudget(Long transactionId, Long userId, String category, BigDecimal amount, String description) {
        try {
            transactionEventProducer.sendEventToBudgetFromTransaction(new TransactionCreatedEventDTO(transactionId, userId, category, amount, TransactionsType.EXPENSE, description));

        } catch (Exception e) {
            throw new RuntimeException("Error Sending Event to Budget", e);
        }
    }
}
