package com.devshaks.personal_finance.kafka.consumer;

import com.devshaks.personal_finance.kafka.data.PaymentTransactionEventDTO;
import com.devshaks.personal_finance.kafka.transaction.PaymentEventProducer;
import com.devshaks.personal_finance.payments.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * KafkaPaymentConsumer is responsible for consuming payment events from a Kafka
 * topic,
 * processing them through the PaymentService, and producing results to another
 * topic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPaymentConsumer {

    // Dependency for handling payment validation logic
    private final PaymentService paymentService;

    // Dependency for producing events back to Kafka topics
    private final PaymentEventProducer paymentEventProducer;

    /**
     * Consumes payment events from the Kafka topic "payment-topic".
     *
     * @param paymentEvent The payment event received from the topic.
     */
    @KafkaListener(topics = "payment-topic", groupId = "paymentGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentEvents(PaymentTransactionEventDTO paymentEvent) {
        try {
            // Log receipt of the payment event
            log.info("Received payment event: {}", paymentEvent);

            // Validate the payment using the PaymentService
            PaymentResponse paymentResponse = paymentService.validatePayment(new PaymentRequest(
                    paymentEvent.transactionId(), paymentEvent.userId(), paymentEvent.amount(), "GBP"));

            // Produce a payment transaction event to the appropriate Kafka topic
            paymentEventProducer.sendEventToTransaction(new PaymentTransactionEventDTO(
                    paymentEvent.transactionId(), paymentEvent.userId(), paymentEvent.amount(),
                    paymentResponse.status()));

        } catch (Exception e) {
            // Log the error for debugging purposes
            log.error("Failed to validate payment: {}", e.getMessage());

            // Handle payment failure by producing a failed payment event
            paymentEventProducer.sendEventToTransaction(new PaymentTransactionEventDTO(
                    paymentEvent.transactionId(),
                    paymentEvent.userId(),
                    paymentEvent.amount(),
                    PaymentStatus.PAYMENT_FAILED));

            // Rethrow the exception with additional context
            throw new IllegalArgumentException("Error while processing payment event", e);
        }
    }
}
