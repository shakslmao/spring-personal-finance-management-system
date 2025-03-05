package com.devshaks.personal_finance.auth.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import com.devshaks.personal_finance.auth.dto.*;
import com.devshaks.personal_finance.exceptions.BusinessException;
import com.devshaks.personal_finance.handlers.BusinessErrorCodes;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.devshaks.personal_finance.security.JwtService;
import com.devshaks.personal_finance.token.Tokens;
import com.devshaks.personal_finance.token.TokensRepository;
import com.devshaks.personal_finance.users.AccountStatus;
import com.devshaks.personal_finance.users.User;
import com.devshaks.personal_finance.users.UserRepository;

import jakarta.mail.MessagingException;

@Slf4j
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokensRepository tokensRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthenticationService(UserRepository userRepository, TokensRepository tokensRepository, JwtService jwtService, AuthenticationManager authenticationManager, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokensRepository = tokensRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public AuthenticationResponse authenticateUser(AuthenticationRequest authRequest) {
        User user = userRepository.findByEmail(authRequest.email())
                .orElseThrow(() -> new BusinessException(BusinessErrorCodes.BAD_CREDENTIALS));

        if (user.getStatus() == AccountStatus.ACTIVE_NON_AUTH) {
            throw new BusinessException(BusinessErrorCodes.ACCOUNT_DISABLED);
        }

        var auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password()));

        var claims = new HashMap<String, Object>();
        var authenticatedUser = (User) auth.getPrincipal();
        claims.put("name", authenticatedUser.getName());

        var jwtToken = jwtService.generateToken(claims, authenticatedUser);
        return new AuthenticationResponse(jwtToken, authenticatedUser.getId());
    }

    public AccountActivationResponse activateUserAccount(String token) throws MessagingException {
        return tokenService.verifyAndActivateAccount(token);
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

    public String extractTokenFromRequest(HttpServletRequest request) {
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


}
