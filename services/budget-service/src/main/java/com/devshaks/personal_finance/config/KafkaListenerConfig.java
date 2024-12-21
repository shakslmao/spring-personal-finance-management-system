package com.devshaks.personal_finance.config;

import com.devshaks.personal_finance.kafka.data.TransactionCreatedEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
public class KafkaListenerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionCreatedEventDTO> kafkaListenerContainerFactory(
            ConsumerFactory<String, TransactionCreatedEventDTO> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TransactionCreatedEventDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, TransactionCreatedEventDTO> consumerFactory() {
        JsonDeserializer<TransactionCreatedEventDTO> deserializer = new JsonDeserializer<>(TransactionCreatedEventDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "budgetGroup");
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

}
