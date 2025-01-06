package com.devshaks.personal_finance.notifications;

import java.util.Map;

public record NotificationRequest(
        String recipientId,
        NotificationType notificationType,
        String notificationMessage,
        Map<String, String> metaData,
        String deviceToken,
        String title,
        Str