package com.devshaks.personal_finance.kafka.audit;

import com.devshaks.personal_finance.kafka.data.AuditBudgetEventDTO;
import com.devshaks.personal_finance.kafka.events.BudgetEvents;
import com.devshaks.personal_finance.kafka.services.ServiceNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventSender {
    private final AuditEventProducer auditEventProducer;

    public void sendEventToAudit(BudgetEvents budgetEvents, Long userId, String description) {
        try {
            auditEventProducer.sendEventToAuditFromBudget(new AuditBudgetEventDTO(
                    budgetEvents,
                    ServiceNames.BUDGET_SERVICE,
                    userId,
                    description,
                    LocalDateTime.now().toString()
            ));

        } catch (Exception kafkaError) {
            throw new RuntimeException("Error Sending the Event to Audit", kafkaError);
        }
    }
}
