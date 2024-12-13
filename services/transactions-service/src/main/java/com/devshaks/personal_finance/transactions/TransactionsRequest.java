package com.devshaks.personal_finance.transactions;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionsRequest(
        @NotNull(message = "User ID is Required")
        Long userId,

        @NotBlank(message = "Category is Required")
        String category,

        @NotNull(message = "Amount is Required")
        @DecimalMin(value = "0.01", message = "Amount Must be Greater than Zero")
        BigDecimal amount,

        @NotBlank(message = "Description is Required")
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description
) {
}
