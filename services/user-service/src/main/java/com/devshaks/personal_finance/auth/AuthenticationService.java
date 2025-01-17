package com.devshaks.personal_finance.auth;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.devshaks.personal_finance.exceptions.UserRegistrationException;
import com.devshaks.personal_finance.handlers.UnauthorizedException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.events.UserEvents;
import com.devshaks.personal_finance.users.User;
import com.devshaks.personal_finance.users.UserDTO;
import com.devshaks.personal_finance.users.UserMapper;
import com.devshaks.personal_finance.users.UserRepository;
import com.devshaks.personal_finance.utility.AgeVerification;
import com.devshaks.personal_finance.utility.UsernameGenerator;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

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
            createKafkaAuditEvent.sendAuditEventFromUser(UserEvents.USER_REGISTERED, user.getId(),
                    "User Registered Successfully");

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

    public AuthenticationResponse authenticateUser(AuthenticationRequest authRequest) {
        return null;
    }

}
