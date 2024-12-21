package com.devshaks.personal_finance.kafka.transactions;

import com.devshaks.personal_finance.kafka.data.TransactionValidatedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionEventProducer {
    private final KafkaTemplate<String, TransactionValidatedEventDTO> kafkaTemplate;

    public void sendEventToTransactionFromBudget(TransactionValidatedEventDTO event) {
        log.info("Sending transaction event to budget topic: {}", event);
        Message<TransactionValidatedEventDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "transaction-validated")
                .setHeader("__TypeId__", TransactionValidatedEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);
    }
}
