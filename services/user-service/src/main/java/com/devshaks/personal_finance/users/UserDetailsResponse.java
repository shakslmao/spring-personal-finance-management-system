package com.devshaks.personal_finance.users;

public record UserDetailsResponse(
        Long userId,
        String firstname,
        String lastname,
        String username,
        String userPin,
        String email,
        UserRoles roles) {
}
