package com.devshaks.personal_finance.kafka.audit;

import com.devshaks.personal_finance.kafka.data.AuditBudgetEventDTO;
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
    private final KafkaTemplate<String, AuditBudgetEventDTO> kafkaTemplate;

    public void sendEventToAuditFromBudget(AuditBudgetEventDTO event) {
        log.info("Sending Budget Event to Audit Service: {}", event);
        Message<AuditBudgetEventDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "budget-topic")
                .setHeader("__TypeId__", AuditBudgetEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);

    }
}
