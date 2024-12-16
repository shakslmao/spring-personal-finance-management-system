package com.devshaks.personal_finance.users;

import com.devshaks.personal_finance.exceptions.TransactionNotFoundException;
import com.devshaks.personal_finance.exceptions.UserNotFoundException;
import com.devshaks.personal_finance.exceptions.UserRegistrationException;
import com.devshaks.personal_finance.handlers.UnauthorizedException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.users.UserEvents;
import com.devshaks.personal_finance.transactions.Transactions;
import com.devshaks.personal_finance.transactions.TransactionsMapper;
import com.devshaks.personal_finance.transactions.TransactionsRepository;
import com.devshaks.personal_finance.transactions.TransactionsResponse;
import com.devshaks.personal_finance.utility.AgeVerification;
import com.devshaks.personal_finance.utility.UsernameGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.stylesheets.LinkStyle;

import static com.devshaks.personal_finance.kafka.users.UserEvents.USER_PASSWORD_RESET_SUCCESS;
import static com.devshaks.personal_finance.kafka.users.UserEvents.USER_REGISTERED;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AgeVerification ageVerification;
    private final AuditEventSender createKafkaAuditEvent;
    private final TransactionsMapper transactionsMapper;
    private final TransactionsRepository transactionsRepository;

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
            validateUserRegistrationRequest(userRegistrationRequest);
            LocalDate dateOfBirth = userRegistrationRequest.dateOfBirth();
            if (!ageVerification.isUserAdult(dateOfBirth)) {
                throw new UserRegistrationException("User must be 18 years or older");
            }
            User user = userMapper.toUserRegistration(userRegistrationRequest);
            String generatedUsername = usernameGenerator.generateUsername(user.getDateOfBirth().getYear());
            user.setUsername(generatedUsername);
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            User savedUser = userRepository.save(user);
            createKafkaAuditEvent.sendAuditEvent(USER_REGISTERED, user.getId(), "User Registered Successfully");
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

    public UserDetailsResponse getUserProfileDetails(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::mapUserToResponse)
                .orElseThrow(() -> new UserNotFoundException("Cannot Find User"));
    }

    public void changeUserPassword(Long userId, @Valid ChangePasswordRequest passwordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with ID: " + userId));
        if (!passwordEncoder.matches(passwordRequest.currentPassword(), user.getPassword())) {
            createKafkaAuditEvent.sendAuditEvent(UserEvents.USER_PASSWORD_RESET_FAILED, user.getId(),
                    "User Password Reset Failed");
            throw new IllegalArgumentException("Current password does not match");
        }
        user.setPassword(passwordEncoder.encode(passwordRequest.newPassword()));
        userRepository.save(user);
        createKafkaAuditEvent.sendAuditEvent(USER_PASSWORD_RESET_SUCCESS, user.getId(),
                "User Password Changed Successfully");
    }

    public List<TransactionsResponse> getUsersTransactions(Long userId) {
        List<Transactions> transactions = transactionsRepository.findByUserId(userId);
        if (transactions.isEmpty()) { throw new TransactionNotFoundException("Cannot Find Transaction For this User"); }
        return transactions.stream().map(transactionsMapper::mapUserToTransactionResponse).toList();
    }


    // Users can Deactivate Account if they have No Funds/Transactions/Etc.

}
