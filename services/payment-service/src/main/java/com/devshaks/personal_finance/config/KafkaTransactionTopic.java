package com.devshaks.personal_finance.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTransactionTopic {
    @Bean
    public NewTopic paymentTransactionTopic() {
        return TopicBuilder.name("payment-topic").build();
    }

}
