package com.devshaks.personal_finance.config;

import com.devshaks.personal_finance.kafka.data.PaymentTransactionEventDTO;
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
    public ConcurrentKafkaListenerContainerFactory<String, PaymentTransactionEventDTO> kafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentTransactionEventDTO> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentTransactionEventDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PaymentTransactionEventDTO> consumerFactory() {
        JsonDeserializer<PaymentTransactionEventDTO> deserializer = new JsonDeserializer<>(PaymentTransactionEventDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "paymentGroup");
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

}
