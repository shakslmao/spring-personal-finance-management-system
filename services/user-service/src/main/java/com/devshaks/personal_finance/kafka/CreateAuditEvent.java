package com.devshaks.personal_finance.kafka;

import com.devshaks.personal_finance.exceptions.AuditEventException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateAuditEvent {
    private final AuditEventProducer auditEventProducer;

    public void sendAuditEvent(UserEvents userEvent, Long userId, String description) {
        try {
            auditEventProducer.sendAuditEvent(new UserEventDTO(
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
