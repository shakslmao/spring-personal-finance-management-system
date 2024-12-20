package com.devshaks.personal_finance.handlers;

import com.devshaks.personal_finance.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandlers {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(UserRegistrationException.class)
    public ResponseEntity<String> handleException(UserRegistrationException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<String> handleException(AdminNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(AdminRegistrationException.class)
    public ResponseEntity<String> handleException(AdminRegistrationException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(AuditEventException.class)
    public ResponseEntity<String> handleException(AuditEventException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleException(BusinessException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<String> handleException(TransactionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException e) {
        var errors = new HashMap<String, String>();
        e.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var fieldName = ((FieldError)error).getField();
                    var errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }
}
