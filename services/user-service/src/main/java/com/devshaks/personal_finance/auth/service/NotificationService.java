package com.devshaks.personal_finance.auth.service;

import com.devshaks.personal_finance.email.EmailService;
import com.devshaks.personal_finance.email.EmailTemplateName;
import com.devshaks.personal_finance.users.User;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Value("${application.mailing.frontend.activation-url}")
    private String activationURL;

    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendAccountActivationEmail(User user, String token) throws MessagingException {
        emailService.sendEmail(user.getEmail(), user.getName(), EmailTemplateName.ACTIVATE_ACCOUNT, activationURL, token , "Account Activation");
    }
}
