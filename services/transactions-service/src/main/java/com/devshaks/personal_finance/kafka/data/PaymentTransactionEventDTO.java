package com.devshaks.personal_finance.kafka.data;

import com.devshaks.personal_finance.transactions.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentTransactionEventDTO(
        @NotNull @JsonProperty("transactionId") Long transactionId,
        @NotNull @JsonProperty("userId") Long userId,
        @NotNull @JsonProperty("amount") BigDecimal amount,
        @NotNull @JsonProperty("status") PaymentStatus status
) {
}
