package com.devshaks.personal_finance.payments;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/validate")
    public ResponseEntity<PaymentResponse> validatePayment(@RequestBody @Valid PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = paymentService.validatePayment(paymentRequest);
        return ResponseEntity.ok(paymentResponse);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signatureHeader) {
        paymentService.processStripeWebhook(payload, signatureHeader);
        return ResponseEntity.ok().build();
    }
}
