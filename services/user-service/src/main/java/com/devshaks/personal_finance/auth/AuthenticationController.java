package com.devshaks.personal_finance.auth;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.devshaks.personal_finance.users.UserDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Controller", description = "Handles Authentication-Related Operations")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Register a new User")
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid UserRegistrationRequest userRegistrationRequest) {
        UserDTO user = authenticationService.registerUser(userRegistrationRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.id())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticateUser(
            @RequestBody @Valid AuthenticationRequest authenticationRequest) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticateUser(authenticationRequest);
        ResponseCookie jwtCookie = authenticationService.generateJwtCookie(authenticationResponse.token());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authenticationResponse);
    }

    @GetMapping("/activate")
    public ResponseEntity<AccountActivationResponse> confirmActivation(@RequestParam AccountActivationRequest accountActivationRequest) throws MessagingException {
        AccountActivationResponse response = authenticationService.activateUserAccount(accountActivationRequest.token());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-new-token")
    public ResponseEntity<RequestNewTokenResponse> requestNewActivationToken(
            @RequestBody @Valid RequestNewActivationRequest newTokenRequest) throws MessagingException {

        RequestNewTokenResponse response = authenticationService.requestNewActivationToken(newTokenRequest.email());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logging out user");
        ResponseCookie jwtCookie = authenticationService.logoutUser(request);
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain("localhost")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.noContent().build();
    }
}
