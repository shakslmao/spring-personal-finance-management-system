package com.devshaks.personal_finance.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Setter
@Getter
@Configuration
public class TwilioConfig {

    @Value("${application.config.TWILIO_ACCOUNT_SID}")
    private String twilioAccountSid;

    @Value("${application.config.TWILIO_AUTH_TOKEN}")
    private String twilioAuthToken;

    @Value("${application.config.TWILIO_PHONE_NUM}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        log.info("Twilio Account SID: {}", twilioAccountSid);
        log.info("Twilio Auth Token: {}", twilioAuthToken);
        log.info("Twilio Phone Number: {}", twilioPhoneNumber);
        if (twilioAccountSid == null || twilioAuthToken == null || twilioPhoneNumber == null) {
            throw new IllegalArgumentException("Twilio Account SID and Auth Token cannot be null");
        }
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }
}
