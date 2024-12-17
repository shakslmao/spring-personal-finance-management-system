package com.devshaks.personal_finance.kafka.consumers;

import com.devshaks.personal_finance.kafka.data.UserBudgetEventDTO;
import com.devshaks.personal_finance.kafka.data.UserTransactionEventDTO;
import com.devshaks.personal_finance.transactions.Transactions;
import com.devshaks.personal_finance.transactions.TransactionsRepository;
import com.devshaks.personal_finance.users.User;
import com.devshaks.personal_finance.users.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaUserConsumer {
    private final UserRepository userRepository;
    private final TransactionsRepository transactionsRepository;
    private final ObjectMapper objectMapper;
    private Map<String, Consumer<Object>> topicHandlerRegistry;

    @KafkaListener(topics = {"transaction-topic", "budget-topic", "notification-topic"}, groupId = "userGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeUserEvent(@Payload String payload, @Header("kafka_receivedTopic") String topic) {
        try {
            log.info("Received Event From: {}: {}", topic, payload);
            Object event = parseEventByTopic(payload, topic);
            processEvent(topic, event);
        } catch (Exception e) {
            // Final catch-all for anything unexpected
            log.error("General error while consuming message from topic {}: {}", topic, e.getMessage(), e);
            // Optionally rethrow or handle in a custom way
        }
    }

    private Object parseEventByTopic(String payload, String topic) throws JsonProcessingException {
        return switch (topic) {
            case "transaction-topic" -> objectMapper.readValue(payload, UserTransactionEventDTO.class);
            case "budget-topic" -> objectMapper.readValue(payload, UserBudgetEventDTO.class);
            default -> throw new IllegalArgumentException("Unsupported Topic: " + topic);
        };
    }

    private void processEvent(String topic, Object event) {
        Consumer<Object> handler = topicHandlerRegistry.getOrDefault(topic, e -> log.warn("No Handler Was Found For Topic: {}, event: {}", topic, e));
        if (handler == null) { throw new IllegalArgumentException("Unknown topic: " + topic); }
        try {
            handler.accept(event);
        } catch (Exception e) {
            log.error("Error processing event for topic {}: {}", topic, e.getMessage(), e);
        }
    }

    @PostConstruct
    public void initMapperRegistry() {
        topicHandlerRegistry = Map.of(
                "transaction-topic", event -> {
                    UserTransactionEventDTO transactionEvent = (UserTransactionEventDTO) event;
                    handleTransactionEvent(transactionEvent);
                },
                "unknown", event -> log.warn("Fallback Handler Invoked for Unsupported Event: {}", event)
                // todo: budget, notification
        );
    }

    public void handleTransactionEvent(@Valid UserTransactionEventDTO event) {
        User user = userRepository.findById(event.userId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Transactions transactions = Transactions.builder()
                .user(user)
                .transactionId(event.transactionId())
                .category(event.category())
                .amount(event.amount())
                .transactionDate(event.transactionDate())
                .transactionType(event.transactionsType())
                .transactionStatus(event.transactionsStatus())
                .build();

        transactionsRepository.save(transactions);
        user.getTransactions().add(transactions);
        userRepository.save(user);
    }
}
