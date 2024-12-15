package com.devshaks.personal_finance.kafka.user;

import com.devshaks.personal_finance.kafka.data.UserTransactionEventDTO;
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
public class UserEventProducer {
    private final KafkaTemplate<String, UserTransactionEventDTO> kafkaTemplate;
    public void sendAuditProducerEventFromTransaction(UserTransactionEventDTO transactionEvents) {
        log.info("Sending Transaction Event to User Service: {}", transactionEvents);
        Message<UserTransactionEventDTO> message = MessageBuilder
                .withPayload(transactionEvents)
                .setHeader(KafkaHeaders.TOPIC, "transaction-topic")
                .setHeader("__typeId__", UserTransactionEventDTO.class.getName())
                .build();
        kafkaTemplate.send(message);
    }

}
