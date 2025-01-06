package com.devshaks.personal_finance.kafka.consumers;

import com.devshaks.personal_finance.audits.Audit;
import com.devshaks.personal_finance.audits.AuditRepository;
import com.devshaks.personal_finance.kafka.data.AuditBudgetEventDTO;
import com.devshaks.personal_finance.kafka.data.AuditTransactionEventDTO;
import com.devshaks.personal_finance.kafka.data.AuditUserEventDTO;
import com.devshaks.personal_finance.kafka.events.EventMapper;
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

    // Listens to multiple Kafka topics and processes events.
    @KafkaListener(topics = { "user-topic", "transaction-topic",
            "budget-topic" }, groupId = "auditGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeAuditEvent(@Payload String payload, @Header("kafka_receivedTopic") String topic) {
        try {
            log.info("Receiving Event From : {}, {}", payload, topic);
            // Parse the incoming payload based on the topic to identify its event type.
            Object event = parseEventByTopic(payload, topic);

            // Convert the parsed event into an Audit object.
            Audit audit = mapToAudit(event);

            // Persist the Audit entity to the database.
            log.info("saving audit event to database");
            auditRepository.save(audit);
            log.info("saved audit event to database: {}", audit);
        } catch (Exception e) {
            // Logs any exception that occurs during processing for debugging purposes.
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Parses the JSON payload into a specific event DTO based on the topic.
     * 
     * @param payload - The raw JSON payload received from Kafka.
     * @param topic   - The topic name the event was published to.
     * @return The parsed event DTO.
     * @throws JsonProcessingException if the payload cannot be deserialized.
     */
    private Object parseEventByTopic(String payload, String topic) throws JsonProcessingException {
        return switch (topic) {
            case "user-topic" -> objectMapper.readValue(payload, AuditUserEventDTO.class);
            case "transaction-topic" -> objectMapper.readValue(payload, AuditTransactionEventDTO.class);
            case "budget-topic" -> objectMapper.readValue(payload, AuditBudgetEventDTO.class);
            default -> throw new IllegalArgumentException("Unsupported Topic: " + topic);
        };
    }

    /**
     * Initialises a registry mapping event DTO types to functions that transform
     * them into Audit objects.
     * This runs after the bean is constructed due to the @PostConstruct annotation.
     */
    @PostConstruct
    public void initMapperRegistry() {
        eventMapperRegistry = Map.of(
                // Mapping for User events.
                AuditUserEventDTO.class, event -> {
                    AuditUserEventDTO userEvent = (AuditUserEventDTO) event;
                    return Audit.builder()
                            .eventType(eventMapper.mapEventToSpecificType(userEvent.eventType())) // Map event type to a
                                                                                                  // standardized value.
                            .serviceName(userEvent.serviceName()) // Set the service that generated the event.
                            .userId(userEvent.userId()) // Set the associated user ID.
                            .description(userEvent.description()) // Add a descriptive message about the event.
                            .timestamp(userEvent.timestamp()) // Timestamp for when the event occurred.
                            .build();
                },
                // Mapping for Transaction events.
                AuditTransactionEventDTO.class, event -> {
                    AuditTransactionEventDTO transactionEvent = (AuditTransactionEventDTO) event;
                    return Audit.builder()
                            .eventType(eventMapper.mapEventToSpecificType(transactionEvent.eventType()))
                            .serviceName(transactionEvent.serviceName())
                            .userId(transactionEvent.userId())
                            .description(transactionEvent.description())
                            .timestamp(transactionEvent.timestamp())
                            .build();
                },
                // Mapping for Budget events.
                AuditBudgetEventDTO.class, event -> {
                    AuditBudgetEventDTO budgetEvent = (AuditBudgetEventDTO) event;
                    return Audit.builder()
                            .eventType(eventMapper.mapEventToSpecificType(budgetEvent.eventType()))
                            .serviceName(budgetEvent.serviceName())
                            .userId(budgetEvent.userId())
                            .description(budgetEvent.description())
                            .timestamp(budgetEvent.timestamp())
                            .build();
                });
    }

    /**
     * Maps a parsed event object to an Audit entity using the registry.
     * 
     * @param event - The parsed event object.
     * @return The Audit entity corresponding to the event.
     * @throws IllegalArgumentException if the event type is not in the registry.
     */
    private Audit mapToAudit(Object event) {
        Function<Object, Audit> mapper = eventMapperRegistry.get(event.getClass());
        if (mapper == null) {
            th