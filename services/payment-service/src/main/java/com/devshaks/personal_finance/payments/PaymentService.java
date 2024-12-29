package com.devshaks.personal_finance.payments;

import com.devshaks.personal_finance.exceptions.PaymentValidationException;
import com.devshaks.personal_finance.transactions.TransactionFeignClient;
import com.devshaks.personal_finance.transactions.TransactionsDTO;
import com.devshaks.personal_finance.transactions.TransactionsStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final TransactionFeignClient transactionFeignClient;

    public PaymentResponse validatePayment(@Valid PaymentRequest paymentRequest) {
        try {
            TransactionsDTO transaction = transactionFeignClient.getTransactionsById(paymentRequest.transactionId());
            if (!transaction.transactionStatus().equals(TransactionsStatus.APPROVED)) {
                throw new IllegalArgumentException(
                        "Transaction is not valid for processing: " + paymentRequest.transactionId());
            }

            if (!transaction.userId().equals(paymentRequest.userId())) {
                throw new IllegalArgumentException(
                        "Transaction does not belong to the user: " + paymentRequest.userId());
            }

            Optional<Payment> existingPayment = paymentRepository
                    .findByTransactionId(paymentRequest.transactionId());
            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                return new PaymentResponse(payment.getPaymentStripeId(), payment.getStatus(),
                        payment.getGatewayResponse());
            }

            // create payment intent.
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.amount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(paymentRequest.currency())
                    .setDescription("Transaction for User " + paymentRequest.userId())
                    .putMetadata("userId", String.valueOf(paymentRequest.userId()))
                    .putMetadata("transactionId", String.valueOf(paymentRequest.transactionId()))
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            String gatewayResponse = paymentIntent.toJson();

            Payment payment = Payment.builder()
                    .paymentStripeId(paymentIntent.getId())
                    .userId(paymentRequest.userId())
                    .transactionId(paymentRequest.transactionId())
                    .amount(paymentRequest.amount())
                    .currency(paymentRequest.currency())
                    .status(PaymentStatus.PAYMENT_PENDING)
                    .gatewayResponse(gatewayResponse)
                    .build();
            paymentRepository.save(payment);

            return new PaymentResponse(paymentIntent.getId(), PaymentStatus.PAYMENT_PENDING, gatewayResponse);
        } catch (StripeException se) {
            log.error("Stripe API error while validating payment: {}", se.getMessage(), se);
            throw new PaymentValidationException("Payment validation failed due to Stripe error: " + se.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while validating payment: {}", e.getMessage(), e);
            throw new PaymentValidationException("Unexpected error during payment validation");
        }
    }

    public void processStripeWebhook(String payload, String signatureHeader) {

    }
}
