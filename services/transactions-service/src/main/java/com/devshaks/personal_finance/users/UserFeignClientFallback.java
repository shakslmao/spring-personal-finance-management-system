package com.devshaks.personal_finance.users;

import org.springframework.stereotype.Component;

@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public UserDetailsResponse getUserProfileDetails(Long id) {
        return new UserDetailsResponse(
                id,
                "Unknown",
                "unknown",
                "unknown@example.com",
                UserRoles.NOT_ASSIGNED
        );
    }
}
