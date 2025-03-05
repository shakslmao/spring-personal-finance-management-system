package com.devshaks.personal_finance.auth.service;

import com.devshaks.personal_finance.auth.dto.UserRegistrationRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final AgeVerification ageVerification;
    private final UserMapper userMapper;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditEventSender auditEventSender;
    private final TokenService tokenService;

    public RegistrationService(UserRepository userRepository, AgeVerification ageVerification, UserMapper userMapper, UsernameGenerator usernameGenerator, PasswordEncoder passwordEncoder, NotificationService notificationService, AuditEventSender auditEventSender, TokenService tokenService) {
        this.userRepository = userRepository;
        this.ageVerification = ageVerification;
        this.userMapper = userMapper;
        this.usernameGenerator = usernameGenerator;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditEventSender = auditEventSender;
        this.tokenService = tokenService;
    }

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
            user.setUserPin(generatedUsername);
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            // Save the user entity and send an audit event.
            User savedUser = userRepository.save(user);
            auditEventSender.sendAuditEventFromUser(UserEvents.USER_REGISTERED, user.getId(),
                    "User Registered Successfully");
            sendActivationEmail(savedUser);
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

    private void sendActivationEmail(User user) throws jakarta.mail.MessagingException {
        String activationToken = tokenService.generateAndSaveActivationToken(user);
        notificationService.sendAccountActivationEmail(user, activationToken);
    }
}
