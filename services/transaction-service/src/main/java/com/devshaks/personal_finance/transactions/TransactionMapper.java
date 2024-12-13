package com.devshaks.personal_finance.transactions;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionMapper {
    public Transactions toNewTransaction(TransactionRequest transactionRequest) {
        return Transactions.builder()
                .userId(transactionRequest.userId())
                .category(transactionRequest.category())
                .amount(transactionRequest.amount())
                .description(transactionRequest.description())
                .tags(TransactionCategoryMapper.getTagsForCategory(transactionRequest.category()))
                .transactionDate(LocalDateTime.now())
                .transactionType(TransactionType.EXPENSE)
                .transactionStatus(TransactionStatus.PENDING)
                .build();
    }

    public TransactionDTO toTransactionDTO(Transactions transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("transaction cannot be null");
        }
        return new TransactionDTO(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getDescription(),
                transaction.getTags());

    }
}
