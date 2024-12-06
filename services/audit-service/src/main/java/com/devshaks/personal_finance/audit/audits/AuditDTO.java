package com.devshaks.personal_finance.audit.audits;

import java.time.LocalDateTime;

public record AuditDTO (
        EventType eventType,
        String serviceName,
        Long userId,
        String description,
        LocalDateTime timestamp
){
}
