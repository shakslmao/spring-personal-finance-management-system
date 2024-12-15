package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AuditTransactionEventDTO(
        @JsonProperty("eventType") TransactionEvents eventType,
        @JsonProperty("serviceName") ServiceNames serviceName,
        @JsonProperty("userId") Long userId,
        @JsonProperty("transactionId") Long transactionId,
        @JsonProperty("description") String description,
        @JsonProperty("timestamp") String timestamp
) {
}
