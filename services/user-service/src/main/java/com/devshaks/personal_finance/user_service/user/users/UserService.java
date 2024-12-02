package com.devshaks.personal_finance.user_service.user.users;

import com.devshaks.personal_finance.user_service.user.utility.UsernameGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;

    private void validateUserRegistrationRequest(@Valid UserRegistrationRequest registrationRequest) {
        if (registrationRequest == null) {
            log.error("User registration request is null");
            throw new IllegalArgumentException("User registration request cannot be null");
        }

        if (userRepository.existsByEmail(registrationRequest.email())) {
            log.error("Email already exists: {}", registrationRequest.email());
            throw new IllegalArgumentException("Email already exists");
        }

        if (registrationRequest.dateOfBirth() == null) {
            log.error("Date of Birth is Null for User: {}", registrationRequest.email());
            throw new IllegalArgumentException("Date of Birth is required");
        }
    }

    @Transactional
    public User registerUser(@Valid UserRegistrationRequest userRegistrationRequest) {
        try {
            validateUserRegistrationRequest(userRegistrationRequest);
            User user = userMapper.toUserRegistration(userRegistrationRequest);
            String generatedUsername = usernameGenerator.generateUsername(user.getDateOfBirth().getYear());
            log.debug("Generated Username: {}", generatedUsername);
            user.setUsername(generatedUsername);

            String encodedPassword = passwordEncoder.encode(user.getPassword());
            log.debug("Password encoded for user: {}", userRegistrationRequest.email());
            user.setPassword(encodedPassword);

            User savedUser = userRepository.save(user);
            log.info("User registered successfully: {}", savedUser.getEmail());

            return savedUser;
        } catch (IllegalArgumentException e) {
            log.error("Validation Error during user registration: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Unexpected error during user registration for email: {}",
                    userRegistrationRequest.email(), ex);
            throw new RuntimeException("Error registering user", ex);
        }
    }

}
