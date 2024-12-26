package com.devshaks.personal_finance.payments;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    public PaymentDTO createPayment(Long userId, @Valid PaymentRequest paymentRequest) {
        return null;
    }
}
