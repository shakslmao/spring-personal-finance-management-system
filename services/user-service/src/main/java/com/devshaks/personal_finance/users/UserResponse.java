package com.devshaks.personal_finance.users;

public record UserResponse(
        Long userId,
        String firstname,
        String username,
        String email,
        UserRoles roles) {
}
