package com.devshaks.personal_finance.admins;

import java.util.Set;

public record AdminDTO(
        Long id,
        String firstname,
        String email,
        String adminCode,
        Set<AdminPermissions> permissions,
        AdminStatus status
) {
}
