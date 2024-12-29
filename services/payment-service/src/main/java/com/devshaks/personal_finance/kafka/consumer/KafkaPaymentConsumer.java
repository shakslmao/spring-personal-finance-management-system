package com.devshaks.personal_finance.kafka.consumer;

import com.devshaks.personal_finance.kafka.data.PaymentTransactionEventDTO;
import com.devshaks.personal_finance.kafka.transaction.PaymentEventProducer;
import com.devshaks.personal_finance.payments.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPaymentConsumer {

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(topics = "payment-topic", groupId = "paymentGroup", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentEvents(PaymentTransactionEventDTO paymentEvent) {
        try {
            PaymentResponse paymentResponse = paymentService.validatePayment(new PaymentRequest(
                    paymentEvent.transactionId(), paymentEvent.userId(), paymentEvent.amount(), "GBP"));

            paymentEventProducer.sendEventToTransaction(new PaymentTransactionEventDTO(
                    paymentEvent.transactionId(), paymentEvent.userId(), paymentEvent.amount(),
                    paymentResponse.status()));

        } catch (Exception e) {
            log.error("Failed to validate payment: {}", e.getMessage());
            paymentEventProducer.sendEventToTransaction(new PaymentTransactionEventDTO(
                    paymentEvent.transactionId(),
                    paymentEvent.userId(),
                    paymentEvent.amount(),
                    PaymentStatus.PAYMENT_FAILED));
            throw new IllegalArgumentException("Error while processing payment event", e);
        }
    }

}
