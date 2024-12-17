package com.devshaks.personal_finance.transactions;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionsMapper {
    public Transactions toNewTransaction(TransactionsRequest transactionRequest) {

        return Transactions.builder()
                .category(transactionRequest.category())
                .amount(transactionRequest.amount())
                .description(transactionRequest.description())
                .tags(TransactionsCategoryMapper.getTagsForCategory(transactionRequest.category()))
                .transactionDate(LocalDateTime.now())
                .transactionType(TransactionsType.EXPENSE)
                .transactionStatus(TransactionsStatus.PENDING)
                .build();
    }


    public TransactionsDTO toTransactionDTO(Transactions transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("transaction cannot be null");
        }
        return new TransactionsDTO(
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


    public TransactionsResponse mapUserToTransactionResponse(Transactions transactions) {
        return new TransactionsResponse(
                transactions.getId(),
                transactions.getUserId(),
                transactions.getCategory(),
                transactions.getAmount(),
                transactions.getTransactionDate(),
                transactions.getTransactionType(),
                transactions.getTransactionStatus(),
                transactions.getDescription()
        );
    }

}
