package com.devshaks.personal_finance.kafka;


import com.fasterxml.jackson.annotation.JsonProperty;

public record AuditEvents(
        @JsonProperty("eventType") EventType eventType,
        @JsonProperty("serviceName") String serviceName,
        @JsonProperty("userId") Long userId,
        @JsonProperty("description") String description,
        @JsonProperty("timestamp") String timestamp
) {
}
