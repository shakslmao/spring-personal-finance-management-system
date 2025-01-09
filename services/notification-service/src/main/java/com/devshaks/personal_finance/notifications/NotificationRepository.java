package com.devshaks.personal_finance.notifications;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientId(String recipientId);

    List<Notification> findByRecipientIdAndNotificationStatus(String recipientId, NotificationStatus status);

}
