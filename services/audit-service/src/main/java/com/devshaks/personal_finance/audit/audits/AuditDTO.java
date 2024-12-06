package com.devshaks.personal_finance.audit.audits;

import java.time.LocalDateTime;

public record AuditDTO (
        String eventType,
        String serviceName,
        String userId,
        String description,
        LocalDateTime timestamp
){
}
