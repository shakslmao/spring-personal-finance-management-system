package com.devshaks.personal_finance.notifications;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devshaks.personal_finance.email.EmailNotificationRequest;
import com.devshaks.personal_finance.pushnotification.PushNotificationRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    public NotificationResponse createPushNotification(PushNotificationRequest request) {
        return null;
    }

    public NotificationResponse createEmailNotification(EmailNotificationRequest request) {
        return null;
    }

    public void sendNotification(String id) {

    }

    public void markNotificationAsRead(String id) {

    }

    public List<NotificationResponse> getNotificationsByRecipient(String recipientId, NotificationStatus status) {
        return null;
    }

}
