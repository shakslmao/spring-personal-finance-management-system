package com.devshaks.personal_finance.payments;

import com.devshaks.personal_finance.exceptions.PaymentValidationException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentResponse validatePayment(@Valid PaymentRequest paymentRequest) {
        try {
            // create payment intent.
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.amount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(paymentRequest.currency())
                    .setDescription("Transaction for User " + paymentRequest.userId())
                    .putMetadata("userId",String.valueOf(paymentRequest.userId()))
                    .putMetadata("transactionId",String.valueOf(paymentRequest.transactionId()))
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
        } catch (Exception e) {
            throw new PaymentValidationException(e.getMessage());
        }
    }

    public void processStripeWebhook(String payload, String signatureHeader) {

    }
}
