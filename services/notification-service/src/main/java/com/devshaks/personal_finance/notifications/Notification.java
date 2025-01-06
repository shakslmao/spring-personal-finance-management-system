package com.devshaks.personal_finance.notifications;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    private String recipientId;
    private NotificationType notificationType;
    private String notificationMessage;
    private Map<String, String> metaData;
    private NotificationStatus notificationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}

// FCM for Notifications - Configure
// Twilio for SMS - Configure
// SES for Email - Configure

// Websockets - FCM Notifications
// Kafka for Email & SMS.
