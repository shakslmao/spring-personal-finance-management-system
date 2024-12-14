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
    private final KafkaTemplate<String, UserEventDTO> kafkaTemplate;
    public void sendAuditEvent(UserEventDTO auditEvents) {
        log.info("Sending User Event to Audit Service: {}", auditEvents);
        Message<UserEventDTO> message = MessageBuilder
                .withPayload(auditEvents)
                .setHeader(KafkaHeaders.TOPIC, "user-topic")
                .setHeader("__TypeId__", UserEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);
    }
}
