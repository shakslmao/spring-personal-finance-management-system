package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.kafka.budget.BudgetEvents;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AuditBudgetEventDTO(
        @JsonProperty("eventType") @NotNull BudgetEvents eventType,
        @JsonProperty("serviceName") @NotNull ServiceNames serviceName,
        @JsonProperty("userId") @NotNull Long userId,
        @JsonProperty("description") @NotNull String description,
        @JsonProperty("timestamp") @NotNull String timestamp)
{
}
