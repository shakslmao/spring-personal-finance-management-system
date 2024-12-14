package com.devshaks.personal_finance.users;

import com.devshaks.personal_finance.config.FeignConfig;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RefreshScope
@FeignClient(name = "user-service", url = "${application.config.user-service-url}", fallback = UserFeignClientFallback.class, qualifiers = "userFeignClient", configuration = FeignConfig.class)
public interface UserFeignClient {
    @GetMapping("/{userId}")
    UserDetailsResponse getUserProfileDetails(@PathVariable("userId") Long id);
}
