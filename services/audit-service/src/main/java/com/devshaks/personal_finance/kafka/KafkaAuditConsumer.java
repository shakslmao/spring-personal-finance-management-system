package com.devshaks.personal_finance.kafka;

import com.devshaks.personal_finance.audits.Audit;
import com.devshaks.personal_finance.audits.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAuditConsumer {
    private final AuditRepository auditRepository;

    @KafkaListener(topics = "user-topic", groupId = "auditGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeAuditEvent(@Payload AuditEvents auditEvents) {
        log.info("Received Audit Event: {}", auditEvents);
        if (auditEvents == null || auditEvents.eventType() == null || auditEvents.serviceName() == null || auditEvents.timestamp() == null) {
            log.warn("Invalid Audit Event received: {}", auditEvents);
            return;
        }
        Audit audit = mapToAudit(auditEvents);
        try {
            auditRepository.save(audit);
        } catch (Exception e) {
            log.error("Error while saving audit event: {}", auditEvents, e);
            throw new RuntimeException(e);
        }
    }


    private Audit mapToAudit(AuditEvents auditEvents) {
       return Audit.builder()
               .eventType(auditEvents.eventType())
               .serviceName(auditEvents.serviceName())
               .userId(auditEvents.userId())
               .description(auditEvents.description())
               .timestamp(auditEvents.timestamp())
               .build();
    }
}
