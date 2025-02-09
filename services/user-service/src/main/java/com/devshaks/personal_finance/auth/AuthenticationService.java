package com.devshaks.personal_finance.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.devshaks.personal_finance.email.EmailService;
import com.devshaks.personal_finance.email.EmailTemplateName;
import com.devshaks.personal_finance.exceptions.UserRegistrationException;
import com.devshaks.personal_finance.handlers.UnauthorizedException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.events.UserEvents;
import com.devshaks.personal_finance.security.JwtService;
import com.devshaks.personal_finance.token.Tokens;
import com.devshaks.personal_finance.token.TokensRepository;
import com.devshaks.personal_finance.users.AccountStatus;
import com.devshaks.personal_finance.users.User;
import com.devshaks.personal_finance.users.UserDTO;
import com.devshaks.personal_finance.users.UserMapper;
import com.devshaks.personal_finance.users.UserRepository;
import com.devshaks.personal_finance.utility.AgeVerification;
import com.devshaks.personal_finance.utility.UsernameGenerator;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository; // Repository for user persistence.
    private final UserMapper userMapper; // Mapper for converting between entities and DTOs.
    private final UsernameGenerator usernameGenerator; // Utility for generating usernames.
    private final PasswordEncoder passwordEncoder; // Utility for encoding and verifying passwords.
    private final AgeVerification ageVerification; // Utility for age validation.
    private final AuditEventSender createKafkaAuditEvent; // Kafka event sender for audit logs.
    private final EmailService emailService;
    private final TokensRepository tokensRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationURL;

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

            sendValidationEmail(savedUser);
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
    
    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(user.getEmail(), user.getName(), EmailTemplateName.ACTIVATE_ACCOUNT, activationURL,
                newToken, "Account Activation");
    }

    public AuthenticationResponse authenticateUser(AuthenticationRequest authRequest) {
        var auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password()));
        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("name", user.getName());
        var jwtToken = jwtService.generateToken(claims, user);
        return new AuthenticationResponse(jwtToken, user.getId());
    }

    public ResponseCookie generateJwtCookie(String token) {
        return ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain("localhost")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);
        var token = Tokens.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        tokensRepository.save(token);
        return generatedToken;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    @Transactional
    public AccountActivationResponse activateUserAccount(String token) throws MessagingException {
        Tokens savedToken = tokensRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation Token has Expired, a New Token Has Been Sent To Your Email");
        }

        User user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User was Not Found"));

        user.setStatus(AccountStatus.ACTIVE_AUTHENTICATED);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        savedToken.setRevoked(true);
        tokensRepository.save(savedToken);
        tokensRepository.deleteByToken(token);
        return new AccountActivationResponse(true, "Account Activated Successfully");
    }

    public ResponseCookie logoutUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            Optional<Tokens> storedToken = tokensRepository.findByToken(token);
            if (storedToken.isPresent()) {
                String refreshToken = storedToken.get().getRefreshToken();
                tokensRepository.revokeTokens(token, refreshToken);
                tokensRepository.deleteByToken(token);
                tokensRepository.deleteByRefreshToken(refreshToken);
            }
        }

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain("localhost")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain("localhost")
                .path("/")
                .maxAge(0)
                .build();

        return jwtCookie;
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Transactional
    public RequestNewTokenResponse requestNewActivationToken(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getStatus() == AccountStatus.ACTIVE_AUTHENTICATED) {
            return new RequestNewTokenResponse("Account is already activated.");
        }
        tokensRepository.findAllValidTokensByUser(user.getId()).forEach(token -> {
            token.setRevoked(true);
        });
        tokensRepository.deleteAllByUserId(user.getId());

        String newToken = generateAndSaveActivationToken(user);
        sendValidationEmail(user);

        return new RequestNewTokenResponse("A new activation token has been sent to your email.");
    }

}
