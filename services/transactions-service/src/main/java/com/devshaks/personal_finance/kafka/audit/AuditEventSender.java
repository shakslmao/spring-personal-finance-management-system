package com.devshaks.personal_finance.kafka.audit;

import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.devshaks.personal_finance.kafka.data.AuditTransactionEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditEventSender {
    private final AuditEventProducer auditEventProducer;
    public void sendEventToAudit(TransactionEvents transactionEvents, Long userId, String description) {
        try {
            auditEventProducer.sendEventToAuditFromTransaction(new AuditTransactionEventDTO(
                    transactionEvents,
                    ServiceNames.TRANSACTION_SERVICE,
                    userId,
                    description,
                    LocalDateTime.now().toString()
            ));
        } catch (Exception kafkaError) {
            throw new RuntimeException("Error Sending the Event to the Audit Service");
        }
    }
}
