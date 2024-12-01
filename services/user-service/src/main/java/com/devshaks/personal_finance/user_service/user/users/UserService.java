package com.devshaks.personal_finance.user_service.user.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    public User registerUser(@Valid UserRegistrationRequest userRegistrationRequest) {

        return null;
    }
}
