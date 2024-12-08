package com.devshaks.personal_finance.admins;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    public AdminDTO registerAdmin(@Valid AdminRegistrationRequest adminRegistrationRequest) {
        return null;
    }
}
