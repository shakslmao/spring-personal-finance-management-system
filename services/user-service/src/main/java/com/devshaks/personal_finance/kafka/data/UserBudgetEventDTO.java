package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.kafka.budget.BudgetEvents;
import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserBudgetEventDTO(
        @JsonProperty("eventType") BudgetEvents eventType,
        @JsonProperty("serviceName") ServiceNames serviceName,
        @JsonProperty("userId") Long userId,
        @JsonProperty("budgetId") Long budgetId,
        @JsonProperty("description") String description,
        @JsonProperty("timestamp") String timestamp
) {
}
