package com.devshaks.personal_finance.audits;

import org.springframework.stereotype.Service;

@Service
public class AuditMapper {
    public AuditDTO toAuditDTO(Audit audit) {
        return new AuditDTO(
                audit.getEventType(),
                audit.getServiceName(),
                audit.getUserId(),
                audit.getDescription(),
                audit.getTimestamp()
        );
    }
}
