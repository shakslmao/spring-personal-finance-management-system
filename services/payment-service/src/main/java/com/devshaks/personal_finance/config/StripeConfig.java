package com.devshaks.personal_finance.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${application.config.STRIPE_API_SECRET_KEY}")
    private String stripeApiSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiSecretKey;
    }
}
