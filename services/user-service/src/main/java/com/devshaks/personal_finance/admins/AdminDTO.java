package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.users.AccountStatus;

import java.util.Set;

public record AdminDTO(
        Long id,
        String firstname,
        String username,
        String email,
        Set<AdminPermissions> permissions,
        AccountStatus status
) {
}
