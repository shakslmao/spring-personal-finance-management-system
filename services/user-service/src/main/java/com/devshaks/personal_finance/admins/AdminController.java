package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.users.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
@Tag(name = "Admin Controller", description = "Handles Admin-Related Operations")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/register")
    //@PreAuthorize("hasRole('')")
    @Operation(summary = "Register a new Admin")
    public ResponseEntity<AdminDTO> registerAdmin(@RequestBody @Valid AdminRegistrationRequest adminRegistrationRequest) {
        AdminDTO admin = adminService.registerAdmin(adminRegistrationRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(admin.id())
                .toUri();
        return ResponseEntity.created(location).body(admin);
    }

    @GetMapping("/users")
    //@PreAuthorize("hasRole('')")
    @Operation(summary = "Get a List of All Users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    //@PreAuthorize("hasRole('')")
    @Operation(summary = "Get Details of One Specific User")
    public ResponseEntity<UserDTO> getUserDetails(@PathVariable("userId") Long userId) {
        UserDTO response = adminService.getUserDetails(userId);
        return ResponseEntity.ok(response);
    }

    // [PATCH] Deactivate a User

    // [PATCH] Reactivate a Deactivated User

    // [DELETE] Permanently Delete a User Account

    // [GET] Get All Transactions (Pagination)

    // [GET] Get All Transactions for User

    // [PATCH] Flag Transactions (e.g., Suspicious Activity)

    // [GET] Retrieve all system activity logs.

    // [GET] Retrieve activity logs for a specific user.

    // [POST] Trigger the generation of detailed system reports (e.g., financial insights, admin activities).

    // [GET] Retrieve system metrics such as user growth, transaction volume, and system performance (Prometheus, Grafana)

    // [GET] Check the health of All Microservices (Super Admin)

    // [POST] Send a System-Wide Notification to All Users.

    // [GET] Retrieve All System Alerts (e.g., Budget Threshold Breaches, Suspicious Transactions)

}
