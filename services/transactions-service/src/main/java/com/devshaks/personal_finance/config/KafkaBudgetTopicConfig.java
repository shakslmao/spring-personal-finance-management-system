package com.devshaks.personal_finance.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaBudgetTopicConfig {
    @Bean
    public NewTopic transactionTopic() { return TopicBuilder.name("transaction-validated").build(); }
}
