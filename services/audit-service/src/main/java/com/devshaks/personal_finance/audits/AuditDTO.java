package com.devshaks.personal_finance.audits;

import com.devshaks.personal_finance.kafka.ServiceNames;

public record AuditDTO (
        EventType eventType,
        ServiceNames serviceName,
        Long userId,
        String description,
        String timestamp
){
}
