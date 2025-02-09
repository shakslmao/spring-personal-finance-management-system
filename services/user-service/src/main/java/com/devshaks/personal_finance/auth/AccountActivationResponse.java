package com.devshaks.personal_finance.auth;

public record AccountActivationResponse(
        boolean success,
        String message
) {
}
