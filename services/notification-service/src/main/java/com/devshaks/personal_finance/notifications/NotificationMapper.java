package com.devshaks.personal_finance.notifications;

import com.devshaks.personal_finance.email.EmailNotificationRequest;
import com.devshaks.personal_finance.pushnotification.PushNotificationRequest;
import com.devshaks.personal_finance.sms.SMSNotificationRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

// sub mod check
@Service
public class NotificationMapper {
    public Notification toNewPushNotification(PushNotificationRequest pushRequest) {
        return Notification.builder()
                .recipientId(pushRequest.deviceToken())
                .notificationType(NotificationType.PUSH)
                .notificationMessage(pushRequest.message())
                .metaData(Map.of("title", pushRequest.title()))
                .notificationStatus(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Notification toNewEmailNotification(EmailNotificationRequest emailRequest) {
        return Notification.builder()
                .recipientId(emailRequest.to())
                .notificationType(NotificationType.EMAIL)
                .notificationMessage(emailRequest.body())
                .metaData(Map.of("from", emailRequest.from()))
                .notificationStatus(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Notification toNewSMSNotification(SMSNotificationRequest smsRequest) {
        return Notification.builder()
                .recipientId(smsRequest.to())
                .notificationType(NotificationType.SMS)
                .notificationMessage(smsRequest.body())
                .notificationStatus(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public NotificationResponse mapToNotificationResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getRecipientId(),
                notification.getNotificationType(), notification.getNotificationMessage(),
                notification.getNotificationStatus(), notification.getCreatedAt(), notification.getSentAt(),
                notification.getReadAt());

    }
}
