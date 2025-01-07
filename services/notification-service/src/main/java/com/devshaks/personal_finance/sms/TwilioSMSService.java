package com.devshaks.personal_finance.sms;

import org.springframework.stereotype.Service;

import com.devshaks.personal_finance.config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwilioSMSService {

    private final TwilioConfig twilioConfig;

    public void sendSMS(String to, String body) {
        try {
            Message message = Message
                    .creator(new PhoneNumber(to), new PhoneNumber(twilioConfig.getTwilioPhoneNumber()), body).create();
            log.info("Sending SMS. SID: {}", message.getSid());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

}
