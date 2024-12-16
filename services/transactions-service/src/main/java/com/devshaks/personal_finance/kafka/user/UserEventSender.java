package com.devshaks.personal_finance.kafka.user;

import com.devshaks.personal_finance.kafka.data.UserTransactionEventDTO;
import com.devshaks.personal_finance.kafka.events.TransactionEvents;
import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.transactions.TransactionsStatus;
import com.devshaks.personal_finance.transactions.TransactionsType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserEventSender {
    private final UserEventProducer userEventProducer;
    public void sendEventToUser(TransactionEvents transactionEvents, Long userId, Long transactionId, String description, String category, BigDecimal amount) {
        try {
            userEventProducer.sendAuditProducerEventFromTransaction(new UserTransactionEventDTO(
                    transactionId,
                    userId,
                    category,
                    amount,
                    LocalDateTime.now().toString(),
                    TransactionsType.EXPENSE,
                    TransactionsStatus.PENDING,
                    description,
                    ServiceNames.TRANSACTION_SERVICE,
                    transactionEvents
            ));
        } catch (KafkaException e) {
            // Catch Kafka-specific exceptions
            throw new RuntimeException("Kafka error sending event: " + e.getMessage(), e);
        } catch (Exception e) {
            // Final catch-all
            throw new RuntimeException("General error sending event to user service: " + e.getMessage(), e);
        }
    }

}
