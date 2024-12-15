package com.devshaks.personal_finance.transactions;

import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.users.UserDetailsResponse;
import com.devshaks.personal_finance.users.UserFeignClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import static com.devshaks.personal_finance.kafka.events.TransactionEvents.TRANSACTION_CREATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionsRepository transactionsRepository;
    private final TransactionsMapper transactionsMapper;
    private final UserFeignClient userFeignClient;
    private final AuditEventSender auditEventSender;

    public TransactionsDTO newTransaction(@Valid TransactionsRequest transactionRequest, Long userId) {
        UserDetailsResponse user = userFeignClient.getUserProfileDetails(userId);
        if (user == null || user.userId() == null) { throw new IllegalArgumentException("User not found"); }
        Transactions transactions = transactionsMapper.toNewTransaction(transactionRequest);
        transactions.setUserId(userId);
        Transactions savedTransaction = transactionsRepository.save(transactions);
        auditEventSender.sendAuditEvent(TRANSACTION_CREATED, userId, transactions.getId(), "New Transaction Created");
        // send kafka event to user microservice about new transaction for user to their profile.
        return transactionsMapper.toTransactionDTO(savedTransaction);
    }
}
