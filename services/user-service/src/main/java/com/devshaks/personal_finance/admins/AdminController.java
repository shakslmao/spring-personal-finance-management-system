package com.devshaks.personal_finance.admins;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
@Tag(name = "Admin Controller", description = "Handles Admin-Related Operations")
public class AdminController {
    private final AdminService adminService;

    @Operation(summary = "Register a new Admin")
    @PostMapping("/register")
    public ResponseEntity<AdminDTO> registerAdmin(@RequestBody @Valid AdminRegistrationRequest adminRegistrationRequest) {
        AdminDTO admin = adminService.registerAdmin(adminRegistrationRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(admin.id())
                .toUri();
        return ResponseEntity.created(location).body(admin);
    }


}
