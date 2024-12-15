package com.devshaks.personal_finance.audits;

import com.devshaks.personal_finance.kafka.services.ServiceNames;

public record AuditDTO (
        Enum<?> eventType,
        ServiceNames serviceName,
        Long userId,
        String description,
        String timestamp
){
}
