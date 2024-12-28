package com.devshaks.personal_finance.payments;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@FeignClient(name = "payment-service", url = "${application.config.payment-service-url}")
public interface PaymentFeignClient {

    @PostMapping("/validate")
    PaymentResponse validatePayment(@RequestBody PaymentRequest paymentRequest);

}
