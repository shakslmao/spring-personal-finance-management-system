package com.devshaks.personal_finance.audit.audits;

import org.springframework.stereotype.Service;

@Service
public class AuditMapper {
    public AuditDTO toAuditDTO(AuditLog auditLog) {
        return new AuditDTO(
                auditLog.getEventType(),
                auditLog.getServiceName(),
                auditLog.getUserId(),
                auditLog.getDescription(),
                auditLog.getTimestamp()
        );
    }
}
