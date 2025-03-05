package com.devshaks.personal_finance.auth.dto;

public record AccountActivationResponse(
        boolean success,
        String message
) {
}
