package com.devshaks.personal_finance.sms;

public record SMSNotificationRequest(
        String to,
        String body) {

}
