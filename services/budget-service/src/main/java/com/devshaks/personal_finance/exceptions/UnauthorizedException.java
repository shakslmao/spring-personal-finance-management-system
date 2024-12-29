package com.devshaks.personal_finance.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
