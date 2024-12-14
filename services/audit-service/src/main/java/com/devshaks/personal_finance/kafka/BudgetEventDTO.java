package com.devshaks.personal_finance.kafka;

import com.devshaks.personal_finance.events.BudgetEvents;
import com.fasterxml.jackson.annotation.JsonProperty;

public record BudgetEventDTO(
        @JsonProperty("eventType") BudgetEvents eventType,
        @JsonProperty("serviceName") ServiceNames serviceName,
        @JsonProperty("userId") Long userId,
        @JsonProperty("budgetId") Long budgetId,
        @JsonProperty("description") String description,
        @JsonProperty("timestamp") String timestamp
) {
}
