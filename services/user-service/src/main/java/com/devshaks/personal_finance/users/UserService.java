package com.devshaks.personal_finance.users;

import com.devshaks.personal_finance.exceptions.UserNotFoundException;
import com.devshaks.personal_finance.exceptions.UserRegistrationException;
import com.devshaks.personal_finance.handlers.UnauthorizedException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.events.UserEvents;
import com.devshaks.personal_finance.utility.AgeVerification;
import com.devshaks.personal_finance.utility.UsernameGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import static com.devshaks.personal_finance.kafka.events.UserEvents.USER_PASSWORD_RESET_SUCCESS;
import static com.devshaks.personal_finance.kafka.events.UserEvents.USER_REGISTERED;

/**
 * Service class for managing user-related operations.
 * Handles user registration, profile retrieval, password changes, and account
 * deactivation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository; // Repository for user persistence.
    private final UserMapper userMapper; // Mapper for converting between entities and DTOs.
    private final UsernameGenerator usernameGenerator; // Utility for generating usernames.
    private final PasswordEncoder passwordEncoder; // Utility for encoding and verifying passwords.
    private final AgeVerification ageVerification; // Utility for age validation.
    private final AuditEventSender createKafkaAuditEvent; // Kafka event sender for audit logs.

    /**
     * Validates a user registration request.
     * Ensures required fields are present and checks for duplicate email.
     */
    private void validateUserRegistrationRequest(@Valid UserRegistrationRequest registrationRequest) {
        if (registrationRequest == null) {
            throw new UserRegistrationException("User registration request cannot be null");
        }
        if (userRepository.existsByEmail(registrationRequest.email())) {
            throw new UserRegistrationException("Email already exists");
        }
        if (registrationRequest.dateOfBirth() == null) {
            throw new UserRegistrationException("Date of Birth is required");
        }
    }

    /**
     * Registers a new user in the system.
     * Performs validation, generates a username, encodes the password, and persists
     * the user entity.
     * Sends an audit event upon successful registration.
     */
    @Transactional
    public UserDTO registerUser(@Valid UserRegistrationRequest userRegistrationRequest) {
        try {
            // Validate the registration request.
            validateUserRegistrationRequest(userRegistrationRequest);

            // Ensure the user meets the age requirement.
            LocalDate dateOfBirth = userRegistrationRequest.dateOfBirth();
            if (!ageVerification.isUserAdult(dateOfBirth)) {
                throw new UserRegistrationException("User must be 18 years or older");
            }

            // Map request to user entity and generate additional fields.
            User user = userMapper.toUserRegistration(userRegistrationRequest);
            String generatedUsername = usernameGenerator.generateUsername(user.getDateOfBirth().getYear());
            user.setUsername(generatedUsername);
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            // Save the user entity and send an audit event.
            User savedUser = userRepository.save(user);
            createKafkaAuditEvent.sendAuditEventFromUser(USER_REGISTERED, user.getId(), "User Registered Successfully");

            return userMapper.toUserDTO(savedUser);
        } catch (HttpClientErrorException exception) {
            // Handle unauthorized exceptions specifically.
            if (exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new UnauthorizedException("Unauthorized Request to Create User", exception);
            }
            throw exception;
        } catch (Exception ex) {
            throw new RuntimeException("Error registering user", ex);
        }
    }

    /**
     * Retrieves user profile details by user ID.
     * Throws an exception if the user is not found.
     */
    public UserDetailsResponse getUserProfileDetails(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::mapUserToResponse)
                .orElseThrow(() -> new UserNotFoundException("Cannot Find User"));
    }

    /**
     * Changes the password for a user.
     * Validates the current password and updates the stored password upon success.
     * Sends appropriate audit events for both success and failure cases.
     */
    public void changeUserPassword(Long userId, @Valid ChangePasswordRequest passwordRequest) {
        // Fetch the user or throw an exception if not found.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with ID: " + userId));

        // Verify the current password matches the stored one.
        if (!passwordEncoder.matches(passwordRequest.currentPassword(), user.getPassword())) {
            createKafkaAuditEvent.sendAuditEventFromUser(UserEvents.USER_PASSWORD_RESET_FAILED, user.getId(),
                    "User Password Reset Failed");
            throw new IllegalArgumentException("Current password does not match");
        }

        // Update and save the new password.
        user.setPassword(passwordEncoder.encode(passwordRequest.newPassword()));
        userRepository.save(user);

        // Send a success audit event.
        createKafkaAuditEvent.sendAuditEventFromUser(USER_PASSWORD_RESET_SUCCESS, user.getId(),
                "User Password Changed Successfully");
    }

    /**
     * Deactivates a user's account if they meet the necessary conditions (e.g., no
     * funds/transactions).
     * Implementation pending further requirements.
     */
    // Users can Deactivate Account if they have No Funds/Transactions/Etc.
}
