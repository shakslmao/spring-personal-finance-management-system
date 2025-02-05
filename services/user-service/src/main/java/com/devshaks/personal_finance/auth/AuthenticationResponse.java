package com.devshaks.personal_finance.auth;

public record AuthenticationResponse(
        String token, Long userId) {
}
