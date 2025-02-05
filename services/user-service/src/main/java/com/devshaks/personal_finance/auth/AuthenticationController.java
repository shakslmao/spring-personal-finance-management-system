package com.devshaks.personal_finance.auth;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
        log.info("Received request to register a new User");
        UserDTO user = authenticationService.registerUser(userRegistrationRequest);
        log.info("Registered new User: {}", user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.id())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticateUser(
            @RequestBody @Valid AuthenticationRequest authenticationRequest) {
        log.info("Logging in User: {}", authenticationRequest);
        AuthenticationResponse authenticationResponse = authenticationService.authenticateUser(authenticationRequest);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authenticationResponse.jwtCookie().toString()).body(authenticationResponse);
    }

    @GetMapping("/activate")
    public void confirmActivation(@RequestParam String token) throws MessagingException {
        authenticationService.activateUserAccount(token);
    }

}
