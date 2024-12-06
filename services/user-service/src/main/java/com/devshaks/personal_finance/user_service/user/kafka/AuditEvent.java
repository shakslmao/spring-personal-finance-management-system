package com.devshaks.personal_finance.user_service.user.kafka;

import java.time.LocalDateTime;

public record AuditEvent(
        EventType eventType,
        String serviceName,
        Long userId,
        String description,
        LocalDateTime timestamp
) {
}
