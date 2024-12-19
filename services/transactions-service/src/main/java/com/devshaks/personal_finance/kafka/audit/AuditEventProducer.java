package com.devshaks.personal_finance.kafka.audit;

import com.devshaks.personal_finance.kafka.data.AuditTransactionEventDTO;
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
    private final KafkaTemplate<String, AuditTransactionEventDTO> kafkaTemplate;

    public void sendEventToAuditFromTransaction(AuditTransactionEventDTO transactionEvents) {
        log.info("Sending Transaction Event To Audit Service: {}", transactionEvents);
        Message<AuditTransactionEventDTO> message = MessageBuilder
                .withPayload(transactionEvents)
                .setHeader(KafkaHeaders.TOPIC, "transaction-topic")
                .setHeader("__TypeId__", AuditTransactionEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);
    }
}
