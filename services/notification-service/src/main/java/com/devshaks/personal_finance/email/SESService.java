package com.devshaks.personal_finance.email;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SESService {
    private final SesClient sesClient;

    public void sendEmailNotification(String to, String subject, String body, String from) {
        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .text(Content.builder().data(body).build())
                                    .build())
                            .build())
                    .source(from)
                    .build();
            sesClient.sendEmail(emailRequest);
            log.info("Email Sending From {}: to: {}", from, to);

        } catch (SesException e) {
            throw new RuntimeException("Failed to send Email: ", e);
        }
    }

}

/*
 * {
 * "to": "recipient@example.com",
 * "from": "verified-sender@example.com",
 * "subject": "Test Notification",
 * "body": "This is a test email sent via Amazon SES."
 * }
 * 
 */