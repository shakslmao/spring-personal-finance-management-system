package com.devshaks.personal_finance.notifications;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    public NotificationResponse createNotification(NotificationRequest request) {
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
