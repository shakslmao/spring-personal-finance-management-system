package com.devshaks.personal_finance.transactions;

import com.devshaks.personal_finance.users.UserClientDTO;
import com.devshaks.personal_finance.users.UserFeignClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionsRepository transactionsRepository;
    private final TransactionsMapper transactionsMapper;
    private final UserFeignClient userFeignClient;

    public TransactionsDTO newTransaction(@Valid TransactionsRequest transactionRequest, Long userId) {
        log.info("Fetching User Details From User Microservice: {}", userId);
        UserClientDTO user = userFeignClient.getUserById(userId);
        if (user == null || user.getId() == null) {
            log.info("User Not Found for User ID: {}", userId);
            throw new IllegalArgumentException("User not found");
        }

        Transactions transactions = transactionsMapper.toNewTransaction(transactionRequest);
        transactions.setUserId(userId);
        Transactions savedTransaction = transactionsRepository.save(transactions);
        return transactionsMapper.toTransactionDTO(savedTransaction);
    }

}
