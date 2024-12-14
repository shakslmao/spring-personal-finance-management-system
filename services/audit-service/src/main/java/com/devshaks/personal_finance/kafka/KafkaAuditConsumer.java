package com.devshaks.personal_finance.kafka;

import com.devshaks.personal_finance.audits.Audit;
import com.devshaks.personal_finance.audits.AuditRepository;
import com.devshaks.personal_finance.events.EventMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAuditConsumer {
    private final AuditRepository auditRepository;
    private final EventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private Map<Class<?>, Function<Object, Audit>> eventMapperRegistry;

    @KafkaListener(topics = {"user-topic", "transaction-topic", "budget-topic"}, groupId = "auditGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeAuditEvent(@Payload String payload, @Header("kafka_receivedTopic") String topic) {
        try {
            Object event = parseEventByTopic(payload, topic);
            Audit audit = mapToAudit(event);
            auditRepository.save(audit);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private Object parseEventByTopic(String payload, String topic) throws JsonProcessingException {
        return switch (topic) {
            case "user-topic" -> objectMapper.readValue(payload, UserEventDTO.class);
            case "transaction-topic" -> objectMapper.readValue(payload, TransactionEventDTO.class);
            case "budget-topic" -> objectMapper.readValue(payload, BudgetEventDTO.class);
            default -> throw new IllegalArgumentException("Unsupported Topic: " + topic);
        };

    }

    @PostConstruct
    public void initMapperRegistry() {
        eventMapperRegistry = Map.of(
                UserEventDTO.class, event -> {
                    UserEventDTO userEvent = (UserEventDTO) event;
                    return Audit.builder()
                            .eventType(eventMapper.mapEventToSpecificType(userEvent.eventType()))
                            .serviceName(userEvent.serviceName())
                            .userId(userEvent.userId())
                            .description(userEvent.description())
                            .timestamp(userEvent.timestamp())
                            .build();
                },
                TransactionEventDTO.class, event -> {
                    TransactionEventDTO transactionEvent = (TransactionEventDTO) event;
                    return Audit.builder()
                            .eventType(eventMapper.mapEventToSpecificType(transactionEvent.eventType()))
                            .serviceName(transactionEvent.serviceName())
                            .userId(transactionEvent.userId())
                            .description(transactionEvent.description())
                            .timestamp(transactionEvent.timestamp())
                            .build();
                },
                BudgetEventDTO.class, event -> {
                    BudgetEventDTO budgetEvent = (BudgetEventDTO) event;
                    return Audit.builder()
                            .eventType(eventMapper.mapEventToSpecificType(budgetEvent.eventType()))
                            .serviceName(budgetEvent.serviceName())
                            .userId(budgetEvent.userId())
                            .description(budgetEvent.description())
                            .timestamp(budgetEvent.timestamp())
                            .build();
                }
        );
    }

    private Audit mapToAudit(Object event) {
        Function<Object, Audit> mapper = eventMapperRegistry.get(event.getClass());
        if (mapper == null) { throw new IllegalArgumentException("Unknown event type: " + event.getClass()); }
        return mapper.apply(event);
    }
}
