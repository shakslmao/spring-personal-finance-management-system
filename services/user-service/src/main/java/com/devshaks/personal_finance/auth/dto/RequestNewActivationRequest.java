package com.devshaks.personal_finance.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestNewActivationRequest(
        @NotBlank(message = "Email is Required")
        @Email(message = "Invalid Email Format")
        String email
) {
}
