package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.transactions.TransactionsStatus;
import com.devshaks.personal_finance.transactions.TransactionsType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record UserTransactionEventDTO(
        @JsonProperty("transactionId") Long transactionId,
        @JsonProperty("userId") Long userId,
        @JsonProperty("category") String category,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("transactionDate") String transactionDate,
        @JsonProperty("transactionType") TransactionsType transactionsType,
        @JsonProperty("transactionStatus") TransactionsStatus transactionsStatus,
        @JsonProperty("description") String description,
        @JsonProperty("serviceNames") ServiceNames serviceNames,
        @JsonProperty("eventType") TransactionEvents eventType
) {
}
