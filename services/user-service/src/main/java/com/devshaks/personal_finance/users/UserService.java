package com.devshaks.personal_finance.users;

import com.devshaks.personal_finance.exceptions.UserNotFoundException;
import com.devshaks.personal_finance.exceptions.UserRegistrationException;
import com.devshaks.personal_finance.handlers.UnauthorizedException;
import com.devshaks.personal_finance.kafka.AuditEvents;
import com.devshaks.personal_finance.kafka.AuditEventProducer;
import com.devshaks.personal_finance.kafka.EventType;
import com.devshaks.personal_finance.kafka.ServiceNames;
import com.devshaks.personal_finance.utility.UsernameGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventProducer auditEventProducer;

    private void validateUserRegistrationRequest(@Valid UserRegistrationRequest registrationRequest) {
        if (registrationRequest == null) { throw new UserRegistrationException("User registration request cannot be null"); }
        if (userRepository.existsByEmail(registrationRequest.email())) { throw new UserRegistrationException("Email already exists"); }
        if (registrationRequest.dateOfBirth() == null) { throw new UserRegistrationException("Date of Birth is required"); }
    }

    /**
     * Register a new User with provided details.
     * 
     * @param userRegistrationRequest The details for the new user registration.
     * @return The registered user details.
     * @throws UnauthorizedException If the request is unauthorized.
     * @throws RuntimeException      If an error occurs while registering the user.
     */
    @Transactional
    public UserDTO registerUser(@Valid UserRegistrationRequest userRegistrationRequest) {
        try {
            validateUserRegistrationRequest(userRegistrationRequest);
            LocalDate dateOfBirth = userRegistrationRequest.dateOfBirth();
            if (!isUserAdult(dateOfBirth)) { throw new UserRegistrationException("User must be 18 years or older"); }
            User user = userMapper.toUserRegistration(userRegistrationRequest);
            String generatedUsername = usernameGenerator.generateUsername(user.getDateOfBirth().getYear());
            user.setUsername(generatedUsername);
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            User savedUser = userRepository.save(user);

            try {
                auditEventProducer.sendAuditEvent(new AuditEvents(
                        EventType.USER_REGISTERED,
                        ServiceNames.USER_SERVICE,
                        savedUser.getId(),
                        "User Registered Successfully",
                        LocalDateTime.now().toString()
                ));

            } catch (Exception kafkaError) {
                log.error("Error Sending Event to Audit.", kafkaError);
                throw new UserRegistrationException("Error Sending Event to Audit Service");
            }
            return userMapper.toUserDTO(savedUser);
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new UnauthorizedException("Unauthorized Request to Create User", exception);
            }
            throw exception;
        } catch (Exception ex) {
            throw new RuntimeException("Error registering user", ex);
        }
    }

    /**
     *
     * @param userId
     * @return
     */
    public UserResponse getUserProfileDetails(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::mapUserToResponse)
                .orElseThrow(() -> new UserNotFoundException("Cannot Find User"));
    }


    // Users can Deactivate Account if they have No Funds/Transactions/Etc.

    /**
     * Check if the user is 18 years or older.
     *
     * @return True if the user is 18 or Older, False otherwise.
     */
    private boolean isUserAdult(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        Period age = Period.between(dateOfBirth, today);
        return age.getYears() >= 18;
    }


}
