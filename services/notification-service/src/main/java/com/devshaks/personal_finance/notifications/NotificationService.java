package com.devshaks.personal_finance.notifications;

import com.devshaks.personal_finance.email.EmailNotificationRequest;
import com.devshaks.personal_finance.exceptions.NotificationNotCreatedException;
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
    }

    public NotificationResponse createEmailNotification(EmailNotificationRequest request) {
        return saveAndRespond(() -> notificationMapper.toNewEmailNotification(request));
    }

    public NotificationResponse createSMSNotification(SMSNotificationRequest request) {
        return saveAndRespond(() -> notificationMapper.toNewSMSNotification(request));
    }

    private NotificationResponse saveAndRespond(Supplier<Notification> notificationSupplier) {
        try {
            Notification notification = notificationSupplier.get();
            validateNotification(notification);
            Notification savedNotification = notificationRepository.save(notification);
            return notificationMapper.mapToNotificationResponse(savedNotification);
        } catch (Exception e) {
            throw new NotificationNotCreatedException("Error, Notification Creation Failed");
        }
    }

    private void validateNotification(Notification notification) {
        if (notification.getRecipientId() == null || notification.getRecipientId().isEmpty()) {
            throw new NotificationNotCreatedException("Recipient ID Must not be Null or Empty!");
        }

        if (notification.getNotificationMessage() == null || notification.getNotificationMessage().isEmpty()) {
            throw new NotificationNotCreatedException("Message Must have a Body to Send!");
        }

    }

    public List<NotificationResponse> getNotificationsByRecipient(String recipientId, NotificationStatus status) {
        return null;
    }

}
