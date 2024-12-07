package com.devshaks.personal_finance.kafka;

import com.devshaks.personal_finance.audits.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;


public record AuditEvents(
        @JsonProperty("eventType") EventType eventType,
        @JsonProperty("serviceName") ServiceNames serviceName,
        @JsonProperty("userId") Long userId,
        @JsonProperty("description") String description,
        @JsonProperty("timestamp") String timestamp
) {

}
