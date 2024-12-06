package com.devshaks.personal_finance.user_service.user.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditEventProducer {
    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;

    public void sendAuditEvent(AuditEvent auditEvent) {
        Message<AuditEvent> message = MessageBuilder
                .withPayload(auditEvent)
                .setHeader(KafkaHeaders.TOPIC, "user-topic")
                .build();
        kafkaTemplate.send(message);
    }

}
