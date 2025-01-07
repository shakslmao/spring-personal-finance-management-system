package com.devshaks.personal_finance.email;

import com.google.firebase.database.annotations.NotNull;

public record EmailNotificationRequest(
        @NotNull String to,
        @NotNull String from,
        @NotNull String subject,
        @NotNull String body) {
}