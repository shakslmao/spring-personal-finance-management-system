package com.devshaks.personal_finance.payments;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Controller", description = "Handles Payment Related Operations")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/new/{userId}")
    @Operation(summary = "Create a new Payment")
    @ApiResponses(value = { @ApiResponse(responseCode = "201")})
    public ResponseEntity<PaymentDTO> createPayment(@PathVariable("userId") Long userId, @Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentDTO payment = paymentService.createPayment(userId, paymentRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(payment.id())
                .toUri();
        return ResponseEntity.created(location).body(payment);
    }
}
