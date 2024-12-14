package com.devshaks.personal_finance.kafka;

import com.devshaks.personal_finance.events.TransactionEvents;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionEventDTO(
        @JsonProperty("eventType") TransactionEvents eventType,
        @JsonProperty("serviceName") ServiceNames serviceName,
        @JsonProperty("userId") Long userId,
        @JsonProperty("transactionId") Long transactionId,
        @JsonProperty("description") String description,
        @JsonProperty("timestamp") String timestamp
) {
}
