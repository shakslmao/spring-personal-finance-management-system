package com.devshaks.personal_finance.transactions;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    public TransactionDTO newTransaction(@Valid TransactionRequest transactionRequest) {
        return null;
    }
}
