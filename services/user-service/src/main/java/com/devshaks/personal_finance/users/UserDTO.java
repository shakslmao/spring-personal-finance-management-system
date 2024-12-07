package com.devshaks.personal_finance.users;

import java.time.LocalDate;

public record UserDTO(
        Long id,
        String firstname,
        String username,
        String email,
        LocalDate dateOfBirth,
        UserRoles roles
) {
}
