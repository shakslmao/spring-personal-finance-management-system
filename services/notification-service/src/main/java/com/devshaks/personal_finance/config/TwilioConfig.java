package com.devshaks.personal_finance.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class TwilioConfig {
    private final String twilioAccountSid = System.getenv("TWILIO_ACCOUNT_SID");
    private final String twilioAuthToken = System.getenv("TWILIO_AUTH_TOKEN");
    private final String twilioPhoneNumber = System.getenv("TWILIO_PHONE_NUM");

    @PostConstruct
    public void init() {
        if (twilioAccountSid == null || twilioAuthToken == null || twilioPhoneNumber == null) {
            throw new IllegalArgumentException("Twilio Account SID and Auth Token cannot be null");
        }
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }
}
