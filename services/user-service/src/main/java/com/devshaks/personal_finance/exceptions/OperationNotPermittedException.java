package com.devshaks.personal_finance.exceptions;

public class OperationNotPermittedException extends RuntimeException {
    public OperationNotPermittedException(String message) {
        super(message);
    }

}
