package com.devshaks.personal_finance.audit.kafka;

import com.devshaks.personal_finance.audit.audits.Audit;
import com.devshaks.personal_finance.audit.audits.AuditDTO;
import com.devshaks.personal_finance.audit.audits.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventConsumer {
    private final AuditRepository auditRepository;

    @KafkaListener(topics = "user-topic", groupId = "audit-service-group")
    public void consumeAuditEvent(AuditDTO auditDTO) {
        log.info("Received Audit Event: {}", auditDTO);
        if (auditDTO == null || auditDTO.eventType() == null || auditDTO.serviceName() == null || auditDTO.timestamp() == null) {
            log.warn("Invalid Audit Event received: {}", auditDTO);
            return;
        }
        processAuditEvent(auditDTO);
    }

    @Async("customTaskExecutor")
    public void processAuditEvent(AuditDTO auditDTO) {
        try {
            Audit audit = mapToAudit(auditDTO);
            auditRepository.save(audit);
            log.info("Successfully Processed Audit Event: {}", audit);
        } catch (Exception e) {
            log.error("Error processing Audit Event: {}", auditDTO, e);
            throw new RuntimeException(e);
        }
    }

    private Audit mapToAudit(AuditDTO auditDTO) {
       return Audit.builder()
               .eventType(auditDTO.eventType())
               .serviceName(auditDTO.serviceName())
               .userId(auditDTO.userId())
               .description(auditDTO.description())
               .timestamp(auditDTO.timestamp())
               .build();
    }

}
