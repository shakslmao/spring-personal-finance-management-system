package com.devshaks.personal_finance.auth.service;

import com.devshaks.personal_finance.auth.dto.AccountActivationResponse;
import com.devshaks.personal_finance.auth.dto.AuthenticationResponse;
import com.devshaks.personal_finance.auth.dto.RequestNewTokenResponse;
import com.devshaks.personal_finance.exceptions.BusinessException;
import com.devshaks.personal_finance.handlers.BusinessErrorCodes;
import com.devshaks.personal_finance.token.Tokens;
import com.devshaks.personal_finance.token.TokensRepository;
import com.devshaks.personal_finance.users.AccountStatus;
import com.devshaks.personal_finance.users.User;
import com.devshaks.personal_finance.users.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class TokenService {
    private final TokensRepository tokensRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TokenService(TokensRepository tokensRepository, UserRepository userRepository, NotificationService notificationService) {
        this.tokensRepository = tokensRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

    public String generateAndSaveActivationToken(User user) {
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

    public String generateActivationCode(int length) {
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

        generateAndSaveActivationToken(user);
        return new RequestNewTokenResponse("A new activation token has been sent to your email.");
    }

    @Transactional
    public AccountActivationResponse verifyAndActivateAccount(String token) throws MessagingException {
        Tokens savedToken = tokensRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            String newToken = generateAndSaveActivationToken(savedToken.getUser());
            notificationService.sendAccountActivationEmail(savedToken.getUser(), newToken);
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
}
