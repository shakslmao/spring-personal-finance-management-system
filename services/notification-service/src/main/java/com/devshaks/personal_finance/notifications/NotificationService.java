package com.devshaks.personal_finance.notifications;

import com.devshaks.personal_finance.email.EmailNotificationRequest;
import com.devshaks.personal_finance.pushnotification.PushNotificationRequest;
import com.devshaks.personal_finance.sms.SMSNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationResponse createPushNotification(PushNotificationRequest request) {
        return saveAndRespond(() -> notificationMapper.toNewPushNotification(request));

        /*
         * Notification notification =
         * notificationMapper.toNewPushNotification(request);
         * Notification savedNotification = notificationRepository.save(notification);
         * return notificationMapper.mapToNotificationResponse(savedNotification);
         */
    }

    public NotificationResponse createEmailNotification(EmailNotificationRequest request) {
        return saveAndRespond(() -> notificationMapper.toNewEmailNotification(request));

        /*
         * Notification notification =
         * notificationMapper.toNewEmailNotification(request);
         * Notification savedNotification = notificationRepository.save(notification);
         * return notificationMapper.mapToNotificationResponse(savedNotification);
         */
    }

    public NotificationResponse createSMSNotification(SMSNotificationRequest request) {
        return saveAndRespond(() -> notificationMapper.toNewSMSNotification(request));

        /*
         * Notification notification = notificationMapper.toNewSMSNotification(request);
         * Notification savedNotification = notificationRepository.save(notification);
         * return notificationMapper.mapToNotificationResponse(savedNotification);
         */
    }

    private NotificationResponse saveAndRespond(Supplier<Notification> notificationSupplier) {
        try {
            Notification notification = notificationSupplier.get();
            validateNotification(notification);
            Notification savedNotification = notificationRepository.save(notification);
            return notificationMapper.mapToNotificationResponse(savedNotification);

        } catch (Exception e) {
            log.error("Failed to create Notification: {}", e.getMessage());
            throw new RuntimeException("Error, Notification Creation Failed", e);
        }
    }

    private void validateNotification(Notification notification) {
        if (notification.getRecipientId() == null || notification.getRecipientId().isEmpty()) {
            throw new IllegalArgumentException("Recipient ID Must not be Null or Empty!");
        }

        if (notification.getNotificationMessage() == null || notification.getNotificationMessage().isEmpty()) {
            throw new IllegalArgumentException("Message Must have a Body to Send!");
        }

    }

    public List<NotificationResponse> getNotificationsByRecipient(String recipientId, NotificationStatus status) {
        return null;
    }

}
