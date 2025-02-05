package com.devshaks.personal_finance.auth;

import org.springframework.http.ResponseCookie;

public record AuthenticationResponse(
        String token, Long userId, ResponseCookie jwtCookie) {
}
