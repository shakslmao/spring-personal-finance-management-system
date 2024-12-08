package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.users.UserRoles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record AdminDTO(
        Long id,
        String firstname,
        String username,
        String email,
        LocalDate dateOfBirth,
        UserRoles roles,
        String adminCode,
        Set<AdminPermissions> permissions,
        LocalDateTime lastAccessedAt,
        AdminStatus status
) {
}
