package com.devshaks.personal_finance.notifications;

import java.time.LocalDateTime;

import com.devshaks.personal_finance.NotificationType;

public record NotificationResponse(
        String id,
        String recipientId,
        NotificationType notificationType,
        String notificationMessage,
        NotificationStatus notificationStatus,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        LocalDateTime readAt

) {

}
