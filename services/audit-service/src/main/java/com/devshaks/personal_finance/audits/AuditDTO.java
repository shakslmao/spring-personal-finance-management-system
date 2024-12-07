package com.devshaks.personal_finance.audits;

import java.time.LocalDateTime;

public record AuditDTO (
        EventType eventType,
        String serviceName,
        Long userId,
        String description,
        String timestamp
){
}
