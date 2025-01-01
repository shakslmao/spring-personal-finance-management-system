package com.devshaks.personal_finance.kafka.consumer;

import com.devshaks.personal_finance.exceptions.TransactionNotFoundException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.data.PaymentTransactionEventDTO;
import com.devshaks.personal_finance.kafka.data.TransactionValidatedEventDTO;
import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.devshaks.personal_finance.transactions.Transactions;
import com.devshaks.personal_finance.transactions.TransactionsRepository;
import com.devshaks.personal_finance.transactions.TransactionsStatus;
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
import java.util.function.Consumer;

/**
 * KafkaTransactionConsumer processes events from various Kafka topics related
 * to transactions and payments.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTransactionConsumer {

    private final TransactionsRepository transactionsRepository;
    private final AuditEventSender auditEventSender;
    private final ObjectMapper objectMapper;

    // Registry of handlers for specific Kafka topics
    private Map<String, Consumer<Object>> topicHandler;

    /**
     * Listens for messages on the specified topics and processes them.
     *
     * @param payload The message payload received from Kafka.
     * @param topic   The Kafka topic the message was received from.
     */
    @KafkaListener(topics = { "transaction-validated",
            "payment-validated" }, groupId = "transactionGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTransactionEvents(@Payload String payload, @Header("kafka_receivedTopic") String topic) {
        try {
            log.info("Received Event From: {}: {}", topic, payload);

            // Parse the payload into the appropriate event type based on the topic
            Object event = parseEventByTopic(payload, topic);

            // Process the event using the appropriate handler
            processEvent(topic, event);
        } catch (Exception e) {
            log.error("General error while consuming message from topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the payload into the appropriate DTO based on the topic.
     *
     * @param payload The message payload as a JSON string.
     * @param topic   The topic the payload belongs to.
     * @return The parsed event object.
     * @throws JsonProcessingException If the payload cannot be parsed.
     */
    private Object parseEventByTopic(String payload, String topic) throws JsonProcessingException {
        return switch (topic) {
            case "transaction-validated" -> objectMapper.readValue(payload, TransactionValidatedEventDTO.class);
            case "payment-validated" -> objectMapper.readValue(payload, PaymentTransactionEventDTO.class);
            default -> throw new IllegalArgumentException("Unknown topic: " + topic);
        };
    }

    /**
     * Processes the event by delegating to the appropriate handler based on the
     * topic.
     *
     * @param topic The topic the event belongs to.
     * @param event The event object to be processed.
     */
    private void processEvent(String topic, Object event) {
        Consumer<Object> handler = topicHandler.getOrDefault(topic, e -> {
            throw new IllegalArgumentException("No handler found for topic: " + topic);
        });
        handler.accept(event);
    }

    /**
     * Initializes the topicHandler registry to map specific topics to their
     * handlers.
     */
    @PostConstruct
    public void initMapperRegistry() {
        topicHandler = Map.of(
                "transaction-validated", event -> {
                    TransactionValidatedEventDTO transactionEvent = (TransactionValidatedEventDTO) event;
                    handleBudgetResponse(transactionEvent);
                },
                "payment-validated", event -> {
                    // Placeholder for handling payment events
                },
                "unknown", event -> log.warn("Fallback Handler for Unsupported Event: {}", event));
    }

    /**
     * Handles events related to budget validation for transactions.
     *
     * @param event The TransactionValidatedEventDTO object.
     */
    private void handleBudgetResponse(TransactionValidatedEventDTO event) {
        Transactions transactions = transactionsRepository.findById(event.transactionId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        // Update transaction status based on the event's success status
        transactions
                .setTransactionStatus(event.isSuccessful() ? TransactionsStatus.APPROVED : TransactionsStatus.REJECTED);

        // Send an audit event based on the success or failure of the transaction
        sendAuditEvent(
                event.isSuccessful() ? TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_APPROVED
                        : TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_REJECTED,
                transactions.getUserId(),
                event.isSuccessful() ? "Approved Transaction With Budget Restriction"
                        : "Denied Transaction With Budget Restriction");

        // Save the updated transaction to the repository
        transactionsRepository.save(transactions);
    }

    /**
     * Sends an audit event for the specified transaction and user.
     *
     * @param eventType The type of audit event to send.
     * @param userId    The user ID associated with the event.
     * @param message   A descriptive message for the event.
     */
    private void sendAuditEvent(TransactionEvents eventType, Long userId, String message) {
        auditEventSender.sendEventToAudit(eventType, userId, message);
    }
}
