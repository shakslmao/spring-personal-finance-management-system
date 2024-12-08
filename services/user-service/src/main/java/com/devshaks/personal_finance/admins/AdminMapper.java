package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.users.User;
import com.devshaks.personal_finance.users.UserRoles;
import org.springframework.stereotype.Service;

@Service
public class AdminMapper {
    public User toAdminRegistration(AdminRegistrationRequest adminRegistrationRequest) {
        if (adminRegistrationRequest == null) { throw new IllegalArgumentException("Admin Registration Request is Required"); }
        if (adminRegistrationRequest.dateOfBirth() == null) { throw new IllegalArgumentException("Date of Birth is Required"); }
        return User.builder()
                .firstname(adminRegistrationRequest.firstname())
                .email(adminRegistrationRequest.email())
                .password(adminRegistrationRequest.password())
                .dateOfBirth(adminRegistrationRequest.dateOfBirth())
                .roles(UserRoles.ADMIN)
                .build();
    }

    public AdminDTO toAdminDTO(Admin admin) {
        return new AdminDTO(
                admin.getId(),
                admin.getFirstname(),
                admin.getEmail(),
                admin.getAdminCode(),
                admin.getPermissions(),
                admin.getStatus()
        );
    }
}
