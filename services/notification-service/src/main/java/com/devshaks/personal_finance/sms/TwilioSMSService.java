package com.devshaks.personal_finance.sms;

import com.devshaks.personal_finance.config.TwilioConfig;
import com.devshaks.personal_finance.exceptions.TwilioSMSException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwilioSMSService {
    private final TwilioConfig twilioConfig;

    public void sendSMS(String to, String body) {
        log.info("Preparing SMS to: {}: {}", to, body);
        try {
            String formattedPhoneNumber = validateAndFormatPhoneNumber(to);
            Message message = Message.creator(
                    new PhoneNumber(formattedPhoneNumber),
                    new PhoneNumber(twilioConfig.getTwilioPhoneNumber()), body).create();
            log.info("Sending SMS. SID: {}", message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}. Error: {}", to, e.getMessage(), e);
            throw new TwilioSMSException("Failed to send SMS");
        }
    }

    private String validateAndFormatPhoneNumber(String phoneNumber) {
        if (!phoneNumber.startsWith("+")) {
            throw new TwilioSMSException("Invalid phone number: " + phoneNumber);
        }
        return phoneNumber.replaceAll("[^+0-9]", "");
    }
}
