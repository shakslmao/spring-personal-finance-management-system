package com.devshaks.personal_finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.util.Value;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SESConfig {

    @Value("${application.config.SES_ACCESS_KEY}")
    private String sesAccessKey;

    @Value("${application.config.SES_SECRET_ACCESS_KEY}")
    private String sesSecretAccessKey;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(sesAccessKey, sesSecretAccessKey)))
                .build();
    }

}
