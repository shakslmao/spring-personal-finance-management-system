package com.devshaks.personal_finance.user;

public record UserDetailsResponse (
        Long userId,
        String firstname,
        String username,
        String email,
        UserRoles roles
)
{
}
