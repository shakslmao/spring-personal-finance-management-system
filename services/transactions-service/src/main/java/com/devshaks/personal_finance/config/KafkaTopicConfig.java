package com.devshaks.personal_finance.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionDLQTopic() {
        return new NewTopic("transaction-dlq", 1, (short) 1);
    }
}
