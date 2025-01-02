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

/**
 * Service class for handling payment validation and Stripe API interactions.
 * Manages payment intents, validation of transactions, and communication with
 * the transaction service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository; // Repository for storing payment records.
    private final TransactionFeignClient transactionFeignClient; // Feign client for fetching transaction details.

    /**
     * Validates a payment request and processes the payment through Stripe.
     * Ensures the associated transaction is valid and that the user owns the
     * transaction.
     * Creates a payment intent with Stripe if no existing payment is found.
     *
     * @param paymentRequest The payment request containing user, transaction, and
     *                       payment details.
     * @return A response containing the payment details and status.
     */
    public PaymentResponse validatePayment(@Valid PaymentRequest paymentRequest) {
        try {
            // Fetch the associated transaction using the transaction ID.
            TransactionsDTO transaction = transactionFeignClient.getTransactionsById(paymentRequest.transactionId());

            // Verify the transaction is approved and belongs to the user.
            if (!transaction.transactionStatus().equals(TransactionsStatus.APPROVED)) {
                throw new IllegalArgumentException(
                        "Transaction is not valid for processing: " + paymentRequest.transactionId());
            }
            if (!transaction.userId().equals(paymentRequest.userId())) {
                throw new IllegalArgumentException(
                        "Transaction does not belong to the user: " + paymentRequest.userId());
            }

            // Check if a payment already exists for the transaction.
            Optional<Payment> existingPayment = paymentRepository.findByTransactionId(paymentRequest.transactionId());
            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                return new PaymentResponse(payment.getPaymentStripeId(), payment.getStatus(),
                        payment.getGatewayResponse());
            }

            // Create a new payment intent with Stripe.
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.amount().multiply(BigDecimal.valueOf(100)).longValue()) // Convert to
                                                                                                      // smallest
                                                                                                      // currency unit.
                    .setCurrency(paymentRequest.currency())
                    .setDescription("Transaction for User " + paymentRequest.userId())
                    .putMetadata("userId", String.valueOf(paymentRequest.userId()))
                    .putMetadata("transactionId", String.valueOf(paymentRequest.transactionId()))
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params); // Call Stripe API to create the intent.
            String gatewayResponse = paymentIntent.toJson(); // Capture the Stripe API response.

            // Save the new payment record in the database.
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

            // Return the response with the payment details and status.
            return new PaymentResponse(paymentIntent.getId(), PaymentStatus.PAYMENT_PENDING, gatewayResponse);
        } catch (StripeException se) {
            // Handle errors from the Stripe API.
            log.error("Stripe API error while validating payment: {}", se.getMessage(), se);
            throw new PaymentValidationException("Payment validation failed due to Stripe error: " + se.getMessage());
        } catch (Exception e) {
            // Handle unexpected errors during payment validation.
            log.error("Unexpected error while validating payment: {}", e.getMessage(), e);
            throw new PaymentValidationException("Unexpected error during payment validation");
        }
    }

    /**
     * Processes incoming Stripe webhook payloads.
     * Handles events like payment success or failure notifications.
     *
     * @param payload         The JSON payload from the Stripe webhook.
     * @param signatureHeader The Stripe signature header for verifying the
     *                        webhook's authenticity.
     */
    public void processStripeWebhook(String payload, String signatureHeader) {
        // TODO: Implement logic for processing Stripe webhooks.
        // This may include verifying the payload, handling specific event types,
        // and updating the payment status in the database.
    }
}
