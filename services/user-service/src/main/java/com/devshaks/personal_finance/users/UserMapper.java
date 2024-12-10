package com.devshaks.personal_finance.users;

import com.devshaks.personal_finance.admins.AdminRegistrationRequest;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public User toUserRegistration(UserRegistrationRequest userRegistrationRequest) {
        if (userRegistrationRequest == null) { throw new IllegalArgumentException("User Registration Request is Required"); }
        if (userRegistrationRequest.dateOfBirth() == null) { throw new IllegalArgumentException("Date of Birth is Required"); }
        return User.builder()
                .firstname(userRegistrationRequest.firstname())
                .email(userRegistrationRequest.email())
                .password(userRegistrationRequest.password())
                .dateOfBirth(userRegistrationRequest.dateOfBirth())
                .roles(UserRoles.USER)
                .build();
        }

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is Required");
        }
        return new UserDTO(
                user.getId(),
                user.getFirstname(),
                user.getUsername(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getRoles()
        );
    }

    public UserResponse mapUserToResponse(User user) {
        if (user == null) { throw new IllegalArgumentException("User is Required"); }
        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }
}
