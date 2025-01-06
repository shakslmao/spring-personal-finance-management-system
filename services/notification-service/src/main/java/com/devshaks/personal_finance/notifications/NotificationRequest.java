package com.devshaks.personal_finance.notifications;

import java.util.Map;

import com.devshaks.personal_finance.NotificationType;

public record NotificationRequest(
        String recipientId,
        NotificationType notificationType,
        String notificationMessage,
        Map<String, String> metaData) {

}
