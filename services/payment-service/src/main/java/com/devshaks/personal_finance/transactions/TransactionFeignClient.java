package com.devshaks.personal_finance.transactions;

import com.devshaks.personal_finance.config.FeignConfig;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RefreshScope
@FeignClient(name = "transaction-service", url = "${application.config.transaction-service-url}", configuration = FeignConfig.class)
public interface TransactionFeignClient {

    @GetMapping("/{id}")
    TransactionsDTO getTransactionsById(@PathVariable("id") Long transactionId);

}