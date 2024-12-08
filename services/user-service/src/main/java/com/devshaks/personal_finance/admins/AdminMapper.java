package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.users.UserRoles;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
public class AdminMapper {
    public Admin toAdminRegistration(AdminRegistrationRequest adminRegistrationRequest) {
        if (adminRegistrationRequest == null) { throw new IllegalArgumentException("Admin Registration Request is Required"); }
        if (adminRegistrationRequest.dateOfBirth() == null) { throw new IllegalArgumentException("Date of Birth is Required"); }
        return Admin.builder()
                .firstname(adminRegistrationRequest.firstname())
                .email(adminRegistrationRequest.email())
                .password(adminRegistrationRequest.password())
                .adminCode(adminRegistrationRequest.adminCode())
                .dateOfBirth(adminRegistrationRequest.dateOfBirth())
                .status(AdminStatus.ACTIVE)
                .roles(UserRoles.ADMIN)
                .build();
    }

    public AdminDTO toAdminDTO(Admin admin) {
        return new AdminDTO(
                admin.getId(),
                admin.getFirstname(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getPermissions() != null ? admin.getPermissions() : EnumSet.noneOf(AdminPermissions.class),
                admin.getStatus()
        );
    }
}
