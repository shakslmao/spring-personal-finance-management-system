package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.kafka.user.UserEvents;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AuditUserEventDTO(
        @JsonProperty("eventType") UserEvents eventType,
        @JsonProperty("serviceName") ServiceNames serviceName,
        @JsonProperty("userId") Long userId,
        @JsonProperty("description") String description,
        @JsonProperty("timestamp") String timestamp
) {

}
