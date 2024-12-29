package com.devshaks.personal_finance.exceptions;

public class PaymentValidationException extends RuntimeException {
    public PaymentValidationException(String message) {
        super(message);
    }
}
