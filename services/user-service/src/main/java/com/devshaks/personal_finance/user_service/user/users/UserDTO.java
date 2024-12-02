package com.devshaks.personal_finance.user_service.user.users;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDTO(
        Long id,
        String firstname,
        String username,
        String email,
        LocalDate dateOfBirth,
        UserRoles roles
) {
}
