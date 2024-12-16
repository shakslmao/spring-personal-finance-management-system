package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.transactions.TransactionsStatus;
import com.devshaks.personal_finance.transactions.TransactionsType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserTransactionEventDTO(
        @NotNull @JsonProperty("transactionId") Long transactionId,
        @NotNull @JsonProperty("userId") Long userId,
        @NotNull @JsonProperty("category") String category,
        @NotNull @JsonProperty("amount") BigDecimal amount,
        @NotNull @JsonProperty("transactionDate") LocalDateTime transactionDate,
        @NotNull @JsonProperty("transactionType") TransactionsType transactionsType,
        @NotNull @JsonProperty("transactionStatus") TransactionsStatus transactionsStatus,
        @NotNull @JsonProperty("transactionDescription") String transactionDescription,
        @NotNull @JsonProperty("eventDescription") String eventDescription,
        @NotNull @JsonProperty("serviceNames") ServiceNames serviceNames,
        @NotNull @JsonProperty("eventType") TransactionEvents eventType
) {
}
