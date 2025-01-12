package com.devshaks.personal_finance.pushnotification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendFCMNotification(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title).setBody(body).build())
                .build();

        CompletableFuture.runAsync(() -> {
            try {
                String response = FirebaseMessaging.getInstance().send(message);
                messagingTemplate.convertAndSend("/topic/notifications", body);

            } catch (Exception e) {
                throw new RuntimeException("Failed to Send Notification", e);
            }
        });
    }
}