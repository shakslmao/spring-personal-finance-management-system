package com.devshaks.personal_finance.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditEventSender {
    private final AuditEventProducer auditEventProducer;
    public void sendAuditEvent(TransactionEvents transactionEvents, Long userId, Long transactionId, String description) {
        try {
            auditEventProducer.sendAuditEventFromTransaction(new TransactionEventDTO(
                    transactionEvents,
                    ServiceNames.TRANSACTION_SERVICE,
                    userId,
                    transactionId,
                    description,
                    LocalDateTime.now().toString()
            ));
        } catch (Exception kafkaError) {
            throw new RuntimeException("Error Sending the Event to the Audit Service");
        }

    }
}
