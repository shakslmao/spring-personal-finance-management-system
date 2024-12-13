package com.devshaks.personal_finance.users;

import org.springframework.stereotype.Component;

@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public UserClientDTO getUserById(Long id) {
        UserClientDTO fallbackResponse = new UserClientDTO();
        fallbackResponse.setId(id);
        fallbackResponse.setFirstname("Unknown");
        fallbackResponse.setEmail("unknown@example.com");
        return fallbackResponse;
    }
}
