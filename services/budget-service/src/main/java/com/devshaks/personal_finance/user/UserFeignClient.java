package com.devshaks.personal_finance.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${application.config.user-service-url}" )
public interface UserFeignClient {
    @GetMapping("/{userId}")
    UserDetailsResponse getUserProfileDetails(@PathVariable("userId") Long userId);
}
