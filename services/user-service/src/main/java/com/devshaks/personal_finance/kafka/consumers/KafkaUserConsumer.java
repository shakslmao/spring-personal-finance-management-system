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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaUserConsumer {
    private final UserRepository userRepository;
    private final TransactionsRepository transactionsRepository;
    private final ObjectMapper objectMapper;

    // Registry mapping topics to their corresponding event handlers
    private Map<String, Consumer<Object>> topicHandlerRegistry;

    /**
     * Kafka listener method to consume events from multiple topics.
     *
     * @param payload The raw JSON payload of the Kafka event.
     * @param topic   The topic from which the event was received.
     */
    @KafkaListener(topics = { "transaction-topic", "budget-topic",
            "notification-topic" }, groupId = "userGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumeUserEvent(@Payload String payload, @Header("kafka_receivedTopic") String topic) {
        try {
            log.info("Received Event From: {}: {}", topic, payload);

            // Parse the event based on the topic
            Object event = parseEventByTopic(payload, topic);

            // Process the parsed event using the corresponding handler
            processEvent(topic, event);
        } catch (Exception e) {
            // Log any unexpected errors during event consumption
            log.error("General error while consuming message from topic {}: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * Parses the raw JSON payload into the appropriate event class based on the
     * topic.
     *
     * @param payload The JSON payload as a string.
     * @param topic   The topic of the Kafka event.
     * @return The parsed event object.
     * @throws JsonProcessingException If the payload cannot be parsed.
     */
    private Object parseEventByTopic(String payload, String topic) throws JsonProcessingException {
        return switch (topic) {
            case "transaction-topic" -> objectMapper.readValue(payload, UserTransactionEventDTO.class);
            case "budget-topic" -> objectMapper.readValue(payload, UserBudgetEventDTO.class);
            default -> throw new IllegalArgumentException("Unsupported Topic: " + topic);
        };
    }

    /**
     * Processes the event by invoking the appropriate handler from the registry.
     *
     * @param topic The topic of the event.
     * @param event The parsed event object.
     */
    private void processEvent(String topic, Object event) {
        Consumer<Object> handler = topicHandlerRegistry.getOrDefault(topic,
                e -> log.warn("No Handler Was Found For Topic: {}, event: {}", topic, e));
        if (handler == null) {
            throw new IllegalArgumentException("Unknown topic: " + topic);
        }
        try {
            // Invoke the handler to process the event
            handler.accept(event);
        } catch (Exception e) {
            // Log errors that occur during event processing
            log.error("Error processing event for topic {}: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * Initializes the handler registry to map topics to their corresponding
     * handlers.
     * This is invoked after the class is constructed.
     */
    @PostConstruct
    public void initMapperRegistry() {
        topicHandlerRegistry = Map.of(
                "transaction-topic", event -> {
                    // Handle transaction events
                    UserTransactionEventDTO transactionEvent = (UserTransactionEventDTO) event;
                    handleTransactionEvent(transactionEvent);
                },
                "unknown", event -> log.warn("Fallback Handler Invoked for Unsupported Event: {}", event)
        // TODO: Add handlers for "budget-topic" and "notification-topic"
        );
    }

    /**
     * Processes a UserTransactionEvent by saving the transaction details
     * and updating the associated user's transaction records.
     *
     * @param event The UserTransactionEventDTO containing transaction details.
     */
    public void handleTransactionEvent(@Valid UserTransactionEventDTO event) {
        // Retrieve the user associated with the transaction
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Create a new Transactions object using the event data
        Transactions transactions = Transactions.builder()
                .user(user)
                .transactionId(event.transactionId())
                .category(event.category())
                .amount(event.amount())
                .transactionDate(event.transactionDate())
                .transactionType(event.transactionsType())
                .transactionStatus(event.transactionsStatus())
                .build();

        // Save the transaction in the repository
        transactionsRepository.save(transactions);

        // Update the user's list of transactions and save the user
        user.getTransactions().add(transactions);
        userRepository.save(user);
    }
}
