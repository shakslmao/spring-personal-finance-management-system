package com.devshaks.personal_finance.user_service.user.users;

import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public User toUserRegistration(UserRegistrationRequest userRegistrationRequest) {
        return User.builder()
                .firstname(userRegistrationRequest.firstname())
                .email(userRegistrationRequest.email())
                .password(userRegistrationRequest.password())
                .dateOfBirth(userRegistrationRequest.dateOfBirth())
                .roles(UserRoles.USER)
                .build();
        }
}
