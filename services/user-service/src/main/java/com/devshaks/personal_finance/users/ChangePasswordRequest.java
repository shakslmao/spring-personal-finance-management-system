package com.devshaks.personal_finance.users;

import jakarta.validation.constraints.NotNull;

public record ChangePasswordRequest(
        @NotNull(message = "Current Password is Required")
        String currentPassword,

        @NotNull(message = "New Password is Required")
        String newPassword
) {
}
