package com.devshaks.personal_finance.kafka.transactions;

import com.devshaks.personal_finance.kafka.data.TransactionValidatedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionEventSender {
    private final TransactionEventProducer transactionEventProducer;
    public void sendEventToTransaction(Long transactionId, Long userId, Boolean isSuccessful, String reason) {
        try {
            transactionEventProducer.sendEventToTransactionFromBudget(new TransactionValidatedEventDTO(
                    transactionId, userId, isSuccessful, reason
            ));
        } catch (KafkaException e) {
            throw new KafkaException(e.getMessage(), e);
        }
    }

}
