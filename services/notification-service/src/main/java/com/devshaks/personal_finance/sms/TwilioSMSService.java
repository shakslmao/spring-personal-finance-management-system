package com.devshaks.personal_finance.sms;

import com.devshaks.personal_finance.config.TwilioConfig;
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
            Message message = Message
                    .creator(new PhoneNumber(to), new PhoneNumber(twilioConfig.getTwilioPhoneNumber()), body).create();
            log.info("Sending SMS. SID: {}", message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

}
