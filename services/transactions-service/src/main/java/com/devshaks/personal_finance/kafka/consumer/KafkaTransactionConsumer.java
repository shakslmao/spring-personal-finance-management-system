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

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTransactionConsumer {
    private final TransactionsRepository transactionsRepository;
    private final AuditEventSender auditEventSender;
    private final ObjectMapper objectMapper;
    private Map<String, Consumer<Object>> topicHandler;

    @KafkaListener(topics = { "transaction-validated", "payment-validated" }, groupId = "transactionGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTransactionEvents(@Payload String payload, @Header("kafka_receivedTopic") String topic) {
        try {
            log.info("Received Event From: {}: {}", topic, payload);
            Object event = parseEventByTopic(payload, topic);
            processEvent(topic, event);
        } catch (Exception e) {
            log.error("General error while consuming message from topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Object parseEventByTopic(String payload, String topic) throws JsonProcessingException {
        return switch (topic) {
            case "transaction-validated" -> objectMapper.readValue(payload, TransactionValidatedEventDTO.class);
            case "payment-validated" -> objectMapper.readValue(payload, PaymentTransactionEventDTO.class);
            default -> throw new IllegalArgumentException("Unknown topic: " + topic);
        };
    }

    private void processEvent(String topic, Object event) {
        Consumer<Object> handler = topicHandler.getOrDefault(topic, e -> {
            throw new IllegalArgumentException("No handler found for topic: " + topic);
        });
        handler.accept(event);
    }

    @PostConstruct
    public void initMapperRegistry() {
        topicHandler = Map.of("transaction-validated", event -> {
            TransactionValidatedEventDTO transactionEvent = (TransactionValidatedEventDTO) event;
            handleBudgetResponse(transactionEvent);
        }, "payment-validated", event -> {

        }, "unknown", event -> log.warn("Fallback Handler for Unsupported Event: {}", event));
    }

    private void handleBudgetResponse(TransactionValidatedEventDTO event) {
        Transactions transactions = transactionsRepository.findById(event.transactionId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        transactions
                .setTransactionStatus(event.isSuccessful() ? TransactionsStatus.APPROVED : TransactionsStatus.REJECTED);
        sendAuditEvent(
                event.isSuccessful() ? TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_APPROVED
                        : TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_REJECTED,
                transactions.getUserId(), event.isSuccessful() ? "Approved Transaction With Budget Restriction"
                        : "Denied Transaction With Budget Restriction");
        transactionsRepository.save(transactions);
    }

    private void sendAuditEvent(TransactionEvents eventType, Long userId, String message) {
        auditEventSender.sendEventToAudit(eventType, userId, message);
    }
}
