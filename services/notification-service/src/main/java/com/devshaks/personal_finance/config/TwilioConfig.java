package com.devshaks.personal_finance.config;

import org.springframework.context.annotation.Configuration;

import com.google.api.client.util.Value;
import com.twilio.Twilio;

import jakarta.annotation.PostConstruct;

@Configuration
public class TwilioConfig {

    @Value("${application.config.TWILIO_ACCOUNT_SID}")
    private String twilioAccountSID;

    @Value("${application.config.TWILIO_AUTH_TOKEN}")
    private String twilioAuthToken;

    @Value("${application.config.TWILIO_PHONE_NUM}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void initializeTwilio() {
        if (twilioAccountSID == null || twilioAuthToken == null) {
            throw new IllegalArgumentException("Credentails are Not Set.");
        }
        Twilio.init(twilioAccountSID, twilioAuthToken);
    }

    public String getTwilioPhoneNumber() {
        return twilioPhoneNumber;
    }

}