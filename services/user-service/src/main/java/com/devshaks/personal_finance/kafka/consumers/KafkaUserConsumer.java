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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
        } catch (JsonProcessingException e) {
            // Thrown by Jackson if JSON parsing fails
            log.error("Error parsing JSON for topic {}: {}", topic, e.getMessage(), e);
            // Optionally: send to a "dead-letter queue," or skip processing
        } catch (EntityNotFoundException | UsernameNotFoundException e) {
            // Common when a user doesn't exist in the DB
            log.error("User not found while processing topic {}: {}", topic, e.getMessage(), e);
            // Optionally handle the missing user scenario
        } catch (ConstraintViolationException e) {
            // Thrown by Hibernate if a DB constraint (unique, not-null, etc.) is violated
            log.error("Constraint violation for topic {}: {}", topic, e.getMessage(), e);
            // Optionally handle or skip
        } catch (DataIntegrityViolationException e) {
            // Often thrown by Spring Data/Hibernate on unique or foreign key violations
            log.error("Data integrity violation for topic {}: {}", topic, e.getMessage(), e);
            // Optionally handle or skip
        } catch (IllegalArgumentException e) {
            // Thrown by parseEventByTopic(...) if the topic is unsupported
            log.error("Invalid topic {} or payload format: {}", topic, e.getMessage(), e);
            // Optionally handle the unknown topic
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
        Consumer<Object> handler = topicHandlerRegistry.get(topic);
        if (handler == null) { throw new IllegalArgumentException("Unknown topic: " + topic); }
        handler.accept(event);
    }

    @PostConstruct
    public void initMapperRegistry() {
        topicHandlerRegistry = Map.of(
                "transaction-topic", event -> {
                    UserTransactionEventDTO transactionEvent = (UserTransactionEventDTO) event;
                    handleTransactionEvent(transactionEvent);
                }
                // todo: budget, notification
        );
    }

    public void handleTransactionEvent(UserTransactionEventDTO event) {
        User user = userRepository.findById(event.userId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Transactions transactions = Transactions.builder()
                .user(user)
                .transactionId(event.transactionId())
                .category(event.category())
                .amount(event.amount())
                .serviceName(event.serviceNames())
                .transactionDate(LocalDateTime.parse(event.transactionDate(), DateTimeFormatter.ISO_DATE_TIME))
                .transactionType(event.transactionsType())
                .transactionStatus(event.transactionsStatus())
                .description(event.description())
                .build();

        transactionsRepository.save(transactions);
        user.getTransactions().add(transactions);
        userRepository.save(user);
    }
}
