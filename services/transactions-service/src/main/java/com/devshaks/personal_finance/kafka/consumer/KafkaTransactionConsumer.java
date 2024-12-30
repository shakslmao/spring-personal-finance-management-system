package com.devshaks.personal_finance.kafka.consumer;

import com.devshaks.personal_finance.exceptions.TransactionNotFoundException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.data.PaymentTransactionEventDTO;
import com.devshaks.personal_finance.kafka.data.TransactionValidatedEventDTO;
import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.devshaks.personal_finance.transactions.PaymentStatus;
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

    @KafkaListener(topics = {"transaction-validated", "payment-validated", "transaction-dlq"}, groupId = "transactionGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTransactionEvents(@Payload String payload, @Header("kafka_receivedTopic") String topic) {
        try {
            log.info("Received Event From: {}: {}", topic, payload);

            if ("transaction-dlq".equals(topic)) {
                handleDLQMessage(payload);
            } else {
                Object event = parseEventByTopic(payload, topic);
                processEvent(topic, event);
            }

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

    private void handleDLQMessage(String payload) {
        try {
            log.info("Processing DLQ Message: {}", payload);

            if (payload.contains("transactionId")) {
                TransactionValidatedEventDTO transactionEvent = objectMapper.readValue(payload, TransactionValidatedEventDTO.class);
                handleBudgetResponse(transactionEvent);
            } else if (payload.contains("status")) {
                PaymentTransactionEventDTO paymentEvent = objectMapper.readValue(payload, PaymentTransactionEventDTO.class);
                handlePaymentResponse(paymentEvent);
            } else {
                log.warn("Unrecognized message format in DLQ: {}", payload);
            }

        } catch (Exception e) {
            log.error("Failed to process message from DLQ: {}. Error: {}", payload, e.getMessage(), e);
        }
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
            PaymentTransactionEventDTO paymentEvent = (PaymentTransactionEventDTO) event;
            handlePaymentResponse(paymentEvent);
        }, "unknown", event -> log.warn("Fallback Handler for Unsupported Event: {}", event));
    }

    private void handleBudgetResponse(TransactionValidatedEventDTO event) {
        Transactions transactions = transactionsRepository.findById(event.transactionId()).orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        transactions.setTransactionStatus(event.isSuccessful() ? TransactionsStatus.APPROVED : TransactionsStatus.REJECTED);
        sendAuditEvent(event.isSuccessful() ? TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_APPROVED : TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_REJECTED, transactions.getUserId(), event.isSuccessful() ? "Approved Transaction With Budget Restriction" : "Denied Transaction With Budget Restriction");
        transactionsRepository.save(transactions);
    }

    private void handlePaymentResponse(PaymentTransactionEventDTO paymentEvent) {
        Transactions transaction = null;
        int attempts = 3;
        while (attempts > 0) {
            try {
                transaction = transactionsRepository.findById(paymentEvent.transactionId()).orElseThrow(() -> new TransactionNotFoundException("Transaction with ID " + paymentEvent.transactionId() + " not found"));
                break;
            } catch (TransactionNotFoundException e) {
                attempts--;
                if (attempts == 0) {
                    log.error("Max retries reached. Transaction with ID {} not found.", paymentEvent.transactionId());
                    throw e;
                }
                log.warn("Retrying to fetch transaction with ID: {} ({} attempts left)", paymentEvent.transactionId(), attempts);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ex);
                }
            }
        }

        if (transaction == null) {
            log.error("Transaction with ID {} could not be fetched after retries.", paymentEvent.transactionId());
            throw new TransactionNotFoundException("Transaction with ID " + paymentEvent.transactionId() + " not found");
        }

        boolean isPaymentSuccessful = paymentEvent.status() == PaymentStatus.PAYMENT_SUCCESSFUL;
        transaction.setTransactionStatus(isPaymentSuccessful ? TransactionsStatus.APPROVED : TransactionsStatus.REJECTED);
        transaction.setPaymentStatus(paymentEvent.status());

        sendAuditEvent(isPaymentSuccessful ? TransactionEvents.TRANSACTION_SUCCESS_PAYMENT_COMPLETED : TransactionEvents.TRANSACTION_FAILED_PAYMENT_REJECTED, transaction.getUserId(), isPaymentSuccessful ? "Successful Payment Validation via Stripe" : "Failed Payment Validation via Stripe");
        transactionsRepository.save(transaction);
    }

    private void sendAuditEvent(TransactionEvents eventType, Long userId, String message) {
        auditEventSender.sendEventToAudit(eventType, userId, message);
    }
}
