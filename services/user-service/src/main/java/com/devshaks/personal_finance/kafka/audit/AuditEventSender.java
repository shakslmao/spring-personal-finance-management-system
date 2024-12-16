package com.devshaks.personal_finance.kafka.audit;

import com.devshaks.personal_finance.exceptions.AuditEventException;
import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.kafka.data.AuditUserEventDTO;
import com.devshaks.personal_finance.kafka.users.UserEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditEventSender {
    private final AuditEventProducer auditEventProducer;

    public void sendAuditEvent(UserEvents userEvent, Long userId, String description) {
        try {
            auditEventProducer.sendAuditEvent(new AuditUserEventDTO(
                    userEvent,
                    ServiceNames.USER_SERVICE,
                    userId,
                    description,
                    LocalDateTime.now().toString()
            ));

        } catch (Exception kafkaError) {
            throw new AuditEventException("Error Sending the Event to the Audit Service");
        }

    }

}
