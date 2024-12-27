package com.devshaks.personal_finance.kafka.payment;

import com.devshaks.personal_finance.kafka.data.PaymentTransactionEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {
    private final KafkaTemplate<String, PaymentTransactionEventDTO> kafkaTemplate;

    public void sendEventToPayment(PaymentTransactionEventDTO paymentEvent) {
        log.info("Sending payment event to payment topic: {}", paymentEvent);
        Message<PaymentTransactionEventDTO> message = MessageBuilder
                .withPayload(paymentEvent)
                .setHeader(KafkaHeaders.TOPIC,"payment-topic")
                .setHeader("__TypeId__", PaymentTransactionEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);
    }
}
