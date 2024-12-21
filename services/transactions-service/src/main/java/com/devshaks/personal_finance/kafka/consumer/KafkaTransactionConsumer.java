package com.devshaks.personal_finance.kafka.consumer;

import com.devshaks.personal_finance.kafka.audit.AuditEventProducer;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.data.TransactionValidatedEventDTO;
import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.devshaks.personal_finance.transactions.Transactions;
import com.devshaks.personal_finance.transactions.TransactionsRepository;
import com.devshaks.personal_finance.transactions.TransactionsStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTransactionConsumer {

    private final TransactionsRepository transactionsRepository;
    private final AuditEventProducer auditEventProducer;
    private final AuditEventSender auditEventSender;

    @KafkaListener(topics = "transaction-validated", groupId = "transactionGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTransactionEvents(TransactionValidatedEventDTO transactionValidated) {
        Transactions transactions = transactionsRepository.findById(transactionValidated.transactionId()).orElse(null);
        assert transactions != null;
        if (transactionValidated.isSuccessful()) {
            transactions.setTransactionStatus(TransactionsStatus.APPROVED);
            auditEventSender.sendEventToAudit(TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_APPROVED, transactions.getUserId(), "Approved Transaction With Budget Restriction");
        } else {
            transactions.setTransactionStatus(TransactionsStatus.REJECTED);
            auditEventSender.sendEventToAudit(TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_REJECTED, transactions.getUserId(), "Denied Transaction With Budget Restriction");
        }
        transactionsRepository.save(transactions);
    }
}
