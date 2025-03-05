package com.devshaks.personal_finance.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountActivationRequest(
        @NotBlank(message = "Activation Token is Required") String token
) {
}
