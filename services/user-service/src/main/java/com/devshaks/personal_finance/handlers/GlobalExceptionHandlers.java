package com.devshaks.personal_finance.handlers;

import com.devshaks.personal_finance.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
    public ResponseEntity<ExceptionResponse> handleException(BusinessException e) {
        BusinessErrorCodes errorCodes = e.getBusinessErrorCode();

        return ResponseEntity.status(errorCodes.getHttpStatusCode())
                .body(ExceptionResponse.builder()
                        .businessErrorCode(errorCodes.getCode())
                        .businessErrorDescription(errorCodes.getDescription())
                        .error(e.getMessage())
                        .build());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<String> handleException(TransactionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse.builder()
                        .businessErrorCode(BusinessErrorCodes.ACCOUNT_LOCKED.getCode())
                        .businessErrorDescription(BusinessErrorCodes.ACCOUNT_LOCKED.getDescription())
                        .error(exception.getMessage())
                        .build());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse.builder()
                        .businessErrorCode(BusinessErrorCodes.ACCOUNT_DISABLED.getCode())
                        .businessErrorDescription(BusinessErrorCodes.ACCOUNT_DISABLED.getDescription())
                        .error(exception.getMessage())
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .businessErrorCode(BusinessErrorCodes.BAD_CREDENTIALS.getCode())
                        .businessErrorDescription(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                        .error(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                        .build());
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.builder()
                        .error(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                        .build());
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleException(OperationNotPermittedException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .error(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException e) {
        var errors = new HashMap<String, String>();
        e.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var fieldName = ((FieldError) error).getField();
                    var errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }
}
