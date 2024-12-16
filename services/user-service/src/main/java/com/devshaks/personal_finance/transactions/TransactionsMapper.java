package com.devshaks.personal_finance.transactions;

import org.springframework.stereotype.Service;

@Service
public class TransactionsMapper {
    public TransactionsResponse mapUserToTransactionResponse(Transactions transactions) {
        return new TransactionsResponse(
                transactions.getUser().getId(),
                transactions.getTransactionId(),
                transactions.getCategory(),
                transactions.getAmount(),
                transactions.getTransactionDate(),
                transactions.getTransactionDescription(),
                transactions.getEventDescription()
        );
    }
}
