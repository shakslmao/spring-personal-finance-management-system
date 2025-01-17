package com.devshaks.personal_finance.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "Handles User-Related Operations")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new User")
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid UserRegistrationRequest userRegistrationRequest) {
        UserDTO user = userService.registerUser(userRegistrationRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.id())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @GetMapping("/{userId}")
    // @PreAuthorize("hasRole('')")
    @Operation(summary = "Response with User Profile Details")
    public ResponseEntity<UserDetailsResponse> getUserProfileDetails(@PathVariable("userId") Long userId) {
        UserDetailsResponse response = userService.getUserProfileDetails(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/change-password")
    // @PreAuthorize("hasRole('')")
    @Operation(summary = "Update a users Password")
    public ResponseEntity<Void> changeUserPassword(@PathVariable("userId") Long userId,
            @RequestBody @Valid ChangePasswordRequest passwordRequest) {
        userService.changeUserPassword(userId, passwordRequest);
        return ResponseEntity.noContent().build();
    }
}
