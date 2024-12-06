package com.devshaks.personal_finance.audit.audits;

public enum EventType {

    // User Lifecycle Events
    USER_REGISTERED,
    USER_DELETED,
    USER_PROFILE_UPDATED,
    USER_ROLE_CHANGED,

    // Authentication Events
    USER_LOGGED_IN,
    USER_LOGIN_FAILED,
    USER_LOGGED_OUT,

    // Registration Events
    USER_REGISTRATION_FAILED,

    // Security Events
    USER_PASSWORD_CHANGED,
    USER_PASSWORD_RESET_REQUESTED,
    USER_PASSWORD_RESET_SUCCESS,
    USER_PASSWORD_RESET_FAILED,
    USER_ACCOUNT_LOCKED,
    USER_ACCOUNT_UNLOCKED,

    // Profile Events
    USER_PROFILE_UPDATE_FAILED

    // Transaction Events

    // Budget Events
}
