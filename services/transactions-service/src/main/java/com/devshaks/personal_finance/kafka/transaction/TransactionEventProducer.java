package com.devshaks.personal_finance.kafka.transaction;

import com.devshaks.personal_finance.kafka.data.TransactionCreatedEventDTO;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEventToBudgetFromTransaction(TransactionCreatedEventDTO transactionEvent) {
        log.info("Sending event to budget from transaction: {}", transactionEvent);
        Message<TransactionCreatedEventDTO> message = MessageBuilder
                .withPayload(transactionEvent)
                .setHeader(KafkaHeaders.TOPIC, "transaction-created")
                .setHeader("__TypeId__",TransactionCreatedEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);
    }
}
