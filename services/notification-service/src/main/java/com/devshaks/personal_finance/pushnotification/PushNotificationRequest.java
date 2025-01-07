package com.devshaks.personal_finance.pushnotification;

public record PushNotificationRequest(
        String deviceToken,
        String title,
        String message) {

}
