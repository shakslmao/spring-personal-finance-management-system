package com.devshaks.personal_finance.budget;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "budget-service", url = "${application.config.budget-service-url}")
public interface BudgetFeignClient {
    @GetMapping("/user/{userId}")
    List<BudgetResponse> getUserBudgets(@PathVariable("userId") Long userId);
}
