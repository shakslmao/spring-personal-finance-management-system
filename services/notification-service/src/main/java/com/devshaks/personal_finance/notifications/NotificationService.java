package com.devshaks.personal_finance.notifications;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devshaks.personal_finance.email.EmailNotificationRequest;
import com.devshaks.personal_finance.pushnotification.PushNotificationRequest;
import com.devshaks.personal_finance.sms.SMSNotificationRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationResponse createPushNotification(PushNotificationRequest request) {
        Notification notification = notificationMapper.toNewPushNotification(request);
        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.mapToNotificationResponse(savedNotification);
    }

    public NotificationResponse createEmailNotification(EmailNotificationRequest request) {
        Notification notification = notificationMapper.toNewEmailNotification(request);
        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.mapToNotificationResponse(savedNotification);
    }

    public NotificationResponse createSMSNotification(SMSNotificationRequest request) {
        Notification notification = notificationMapper.toNewSMSNotification(request);
        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.mapToNotificationResponse(savedNotification);
    }

    public List<NotificationResponse> getNotificationsByRecipient(String recipientId, NotificationStatus status) {
        return null;
    }

}
