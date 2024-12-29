package com.devshaks.personal_finance.kafka.transaction;

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

    public void sendEventToTransaction(PaymentTransactionEventDTO transactionEvent) {
        log.info("Sending event to budget from transaction: {}", transactionEvent);
        Message<PaymentTransactionEventDTO> message = MessageBuilder
                .withPayload(transactionEvent)
                .setHeader(KafkaHeaders.TOPIC, "payment-validated")
                .setHeader("__TypeId__",PaymentTransactionEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);
    }
}
