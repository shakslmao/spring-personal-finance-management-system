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

    @KafkaListener(topics = {"transaction-validated", "payment-validated" }, groupId = "transactionGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTransactionEvents(@Payload String payload, @Header("kafka_receivedTopic") String topic) {
        try {
            log.info("Received Event From: {}: {}", topic, payload);
            Object event = parseEventByTopic(payload,topic);
            processEvent(topic,event);
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
        topicHandler = Map.of(
                "transaction-validated", event -> {
                    TransactionValidatedEventDTO transactionEvent = (TransactionValidatedEventDTO) event;
                    handleBudgetResponse(transactionEvent);
                },
                "payment-validated", event -> {
                    PaymentTransactionEventDTO paymentEvent = (PaymentTransactionEventDTO) event;
                    handlePaymentResponse(paymentEvent);
                },
                "unknown", event -> log.warn("Fallback Handler for Unsupported Event: {}", event)
        );
    }

    private void handleBudgetResponse(TransactionValidatedEventDTO event) {
        Transactions transactions = transactionsRepository.findById(event.transactionId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        if (event.isSuccessful()) {
            transactions.setTransactionStatus(TransactionsStatus.APPROVED);
            auditEventSender.sendEventToAudit(TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_APPROVED, transactions.getUserId(), "Approved Transaction With Budget Restriction");
        } else {
            transactions.setTransactionStatus(TransactionsStatus.REJECTED);
            auditEventSender.sendEventToAudit(TransactionEvents.TRANSACTION_BUDGET_RESTRICTION_REJECTED, transactions.getUserId(), "Denied Transaction With Budget Restriction");
        }
        transactionsRepository.save(transactions);
    }

    private void handlePaymentResponse(PaymentTransactionEventDTO paymentEvent) {
        Transactions transaction = transactionsRepository.findById(paymentEvent.transactionId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        if (paymentEvent.status() == PaymentStatus.PAYMENT_SUCCESSFUL) {
            transaction.setTransactionStatus(TransactionsStatus.APPROVED);
            transaction.setPaymentStatus(PaymentStatus.PAYMENT_SUCCESSFUL);
            auditEventSender.sendEventToAudit(TransactionEvents.TRANSACTION_SUCCESS_PAYMENT_COMPLETED, transaction.getUserId(), "Successful Payment Validation via Stripe");
            transactionsRepository.save(transaction);
        } else {
            transaction.setTransactionStatus(TransactionsStatus.REJECTED);
            transaction.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
            auditEventSender.sendEventToAudit(TransactionEvents.TRANSACTION_FAILED_PAYMENT_REJECTED, transaction.getUserId(), "Failed Payment Validation via Stripe");
            transactionsRepository.save(transaction);
        }
    }
}
