package com.devshaks.personal_finance.services;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FCMService {

    public void sendFCMNotification(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title).setBody(body).build())
                .build();

        try {
            var response = FirebaseMessaging.getInstance().sendAsync(message);
            log.info("Successfully Sent FCM Notification: " + response);

        } catch (Exception e) {
            log.error(body, e);
            throw new RuntimeException("Failed to Send Notification", e);
        }

    }

}