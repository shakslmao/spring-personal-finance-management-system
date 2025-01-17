package com.devshaks.personal_finance.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserRegistrationRequest(

        @NotNull(message = "First Name is Required") String firstname,
        @NotNull(message = "Last Name is Required") String lastname,
        @NotNull(message = "Email is Required") @Email(message = "Email is Invalid") String email,
        @NotNull(message = "Password is Required") @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters") @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one number") @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one number") @Pattern(regexp = ".*[@$!%*#?&].*", message = "Password must contain at least one special character") String password,
        @NotNull(message = "Date of Birth is Required") @Past(message = "Date of Birth must be in the past") LocalDate dateOfBirth) {
}
