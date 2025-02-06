package com.devshaks.personal_finance.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "Handles User-Related Operations")
public class UserController {
    private final UserService userService;

    @GetMapping("/userprofiles")
    // @PreAuthorize("hasRole('')")
    @Operation(summary = "Response with User Profile Details")
    public ResponseEntity<UserDetailsResponse> getLoggedInUserProfile(@AuthenticationPrincipal User user) {
        UserDetailsResponse response = userService.getUserProfileDetails(user.getId());
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
