package com.devshaks.personal_finance.kafka.payment;

import com.devshaks.personal_finance.kafka.data.PaymentTransactionEventDTO;
import com.devshaks.personal_finance.transactions.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentEventSender {
    private  final PaymentEventProducer paymentEventProducer;

    public void sendEventToPayment(Long userId, Long transactionId, BigDecimal amount) {
        try {
            paymentEventProducer.sendEventToPayment(new PaymentTransactionEventDTO(
                    userId, transactionId, amount, PaymentStatus.PAYMENT_PENDING
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
