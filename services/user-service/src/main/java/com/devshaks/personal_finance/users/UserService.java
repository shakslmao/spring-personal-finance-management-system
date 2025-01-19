package com.devshaks.personal_finance.users;

import com.devshaks.personal_finance.exceptions.UserNotFoundException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.events.UserEvents;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.devshaks.personal_finance.kafka.events.UserEvents.USER_PASSWORD_RESET_SUCCESS;

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
    private final PasswordEncoder passwordEncoder; // Utility for encoding and verifying passwords.
    private final AuditEventSender createKafkaAuditEvent; // Kafka event sender for audit logs.

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
