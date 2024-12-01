package com.devshaks.personal_finance.user_service.user.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserRegistrationRequest(

        @NotNull(message = "First Name is Required")
        String firstname,

        @NotNull(message = "Email is Required")
        @Email(message = "Email is Invalid")
        String email,

        @NotNull(message = "Password is Required")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        String password,

        @NotNull(message = "Date of Birth is Required")
        @Past(message = "Date of Birth must be in the past")
        LocalDate dateOfBirth
) {
}
