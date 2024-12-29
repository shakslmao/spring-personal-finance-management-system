package com.devshaks.personal_finance.transactions;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "transaction-service", url = "${application.config.transaction-service-url}")
public interface TransactionFeignClient {

    @GetMapping("/{id}")
    TransactionsDTO getTransactionsById(@PathVariable("id") Long transactionId);

}