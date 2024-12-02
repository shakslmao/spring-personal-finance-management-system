package com.devshaks.personal_finance.user_service.user.users;

import com.devshaks.personal_finance.user_service.user.exceptions.UserRegistrationException;
import com.devshaks.personal_finance.user_service.user.handlers.UnauthorizedException;
import com.devshaks.personal_finance.user_service.user.utility.UsernameGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
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

    private void validateUserRegistrationRequest(@Valid UserRegistrationRequest registrationRequest) {
        if (registrationRequest == null) {
            log.error("User registration request is null");
            throw new UserRegistrationException("User registration request cannot be null");
        }

        if (userRepository.existsByEmail(registrationRequest.email())) {
            log.error("Email already exists: {}", registrationRequest.email());
            throw new UserRegistrationException("Email already exists");
        }

        if (registrationRequest.dateOfBirth() == null) {
            log.error("Date of Birth is Null for User: {}", registrationRequest.email());
            throw new UserRegistrationException("Date of Birth is required");
        }
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
            if (!isUserAdult(dateOfBirth)) {
                throw new UserRegistrationException("User must be 18 years or older");
            }
            User user = userMapper.toUserRegistration(userRegistrationRequest);
            String generatedUsername = usernameGenerator.generateUsername(user.getDateOfBirth().getYear());

            user.setUsername(generatedUsername);
            String encodedPassword = passwordEncoder.encode(user.getPassword());

            user.setPassword(encodedPassword);
            User savedUser = userRepository.save(user);
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
     * Check if the user is 18 years or older.
     * 
     * @param dateOfBirth
     * @return True if the user is 18 or Older, False otherwise.
     */
    private boolean isUserAdult(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        Period age = Period.between(dateOfBirth, today);
        return age.getYears() >= 18;
    }

}
