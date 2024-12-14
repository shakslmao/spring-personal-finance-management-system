package com.devshaks.personal_finance.audits;

import com.devshaks.personal_finance.events.EventType;
import com.devshaks.personal_finance.events.UserEvents;
import com.devshaks.personal_finance.kafka.ServiceNames;

public record AuditDTO (
        Enum<?> eventType,
        ServiceNames serviceName,
        Long userId,
        String description,
        String timestamp
){
}
