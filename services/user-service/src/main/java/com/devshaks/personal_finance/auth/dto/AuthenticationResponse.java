package com.devshaks.personal_finance.auth.dto;


public record AuthenticationResponse(
        String token, Long userId) {
}
