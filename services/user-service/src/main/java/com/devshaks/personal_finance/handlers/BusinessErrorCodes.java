package com.devshaks.personal_finance.handlers;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum BusinessErrorCodes {
    NO_CODE(0, HttpStatus.NOT_IMPLEMENTED, "No Code"),
    INCORRECT_CURRENT_PASSWORD(300, HttpStatus.BAD_REQUEST, "Incorrect Current Password"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, HttpStatus.BAD_REQUEST, "New Password Does Not Match"),
    ACCOUNT_LOCKED(302, HttpStatus.FORBIDDEN, "Account Is Locked"),
    ACCOUNT_DISABLED(303, HttpStatus.FORBIDDEN, "Account Is Disabled"),
    BAD_CREDENTIALS(304, HttpStatus.FORBIDDEN, "Login And Or Password Credentials Are Incorrect");

    private final int code;
    private final String description;
    private final HttpStatus httpStatusCode;

    BusinessErrorCodes(int code, HttpStatus httpStatusCode, String description) {
        this.code = code;
        this.description = description;
        this.httpStatusCode = httpStatusCode;
    }
}
