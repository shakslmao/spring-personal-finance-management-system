package com.devshaks.personal_finance.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @NotEmpty(message = "Email is Required") @NotBlank(message = "Email is Required") @Email(message = "Email is Invalid") String email,
        @NotNull(message = "Password is Required") @Size(min = 8, max = 20, message = "Password Must be Between 8 and 20 Characters") @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one number") @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one number") @Pattern(regexp = ".*[@$!%*#?&].*", message = "Password must contain at least one special character") String password) {

}
