package com.devshaks.personal_finance.notifications;

import java.util.Map;

import com.google.firebase.database.annotations.NotNull;

public record NotificationRequest(
        @NotNull String recipientId,
        @NotNull NotificationType notificationType,
        @NotNull String notificationMessage,
        @NotNull Map<String, String> metaData) {

}
