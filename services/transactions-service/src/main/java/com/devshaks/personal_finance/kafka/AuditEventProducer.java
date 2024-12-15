package com.devshaks.personal_finance.kafka;

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
public class AuditEventProducer {
    private final KafkaTemplate<String, TransactionEventDTO> kafkaTemplate;

    public void sendAuditEventFromTransaction(TransactionEventDTO transactionEvents) {
        log.info("Sending Transaction Event To Audit Service: {}", transactionEvents);
        Message<TransactionEventDTO> message = MessageBuilder
                .withPayload(transactionEvents)
                .setHeader(KafkaHeaders.TOPIC, "transaction-topic")
                .setHeader("__TypeId__", TransactionEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);
    }
}
