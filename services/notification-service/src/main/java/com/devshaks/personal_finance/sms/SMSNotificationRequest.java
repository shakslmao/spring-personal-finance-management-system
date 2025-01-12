package com.devshaks.personal_finance.sms;

import jakarta.validation.constraints.NotBlank;

public record SMSNotificationRequest(
        @NotBlank(message = "Recipient ID cannot be blank") String to,
        @NotBlank(message = "Message Body cannot be blank") String body) {
}
