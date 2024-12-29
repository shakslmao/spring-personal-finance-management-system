package com.devshaks.personal_finance.users;

public record UserDetailsResponse (
        Long userId,
        String firstname,
        String username,
        String email,
        UserRoles roles)
{
}
