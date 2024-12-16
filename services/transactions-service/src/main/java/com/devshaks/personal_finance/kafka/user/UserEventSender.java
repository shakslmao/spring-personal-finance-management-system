package com.devshaks.personal_finance.kafka.user;

import com.devshaks.personal_finance.kafka.data.UserTransactionEventDTO;
import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.transactions.TransactionsStatus;
import com.devshaks.personal_finance.transactions.TransactionsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventSender {
    private final UserEventProducer userEventProducer;
    public void sendEventToUser(TransactionEvents transactionEvents, Long userId, Long transactionId, String eventDescription, String userDescription, String category, BigDecimal amount){
        try {
            userEventProducer.sendUserProducerEventFromTransaction(new UserTransactionEventDTO(
                    transactionId,
                    userId,
                    category,
                    amount,
                    LocalDateTime.now(),
                    TransactionsType.EXPENSE,
                    TransactionsStatus.PENDING,
                    userDescription,
                    eventDescription,
                    ServiceNames.TRANSACTION_SERVICE,
                    transactionEvents
            ));
        } catch (KafkaException e) {
            throw new RuntimeException("Kafka error sending event: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("General error sending event to user service: " + e.getMessage(), e);
        }
    }

}
