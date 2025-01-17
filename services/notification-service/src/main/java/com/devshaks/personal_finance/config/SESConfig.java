package com.devshaks.personal_finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SESConfig {
    private final String sesAccessKey = "";
    private final String sesSecretAccessKey = "";

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(sesAccessKey, sesSecretAccessKey)))
                .build();
    }
}
