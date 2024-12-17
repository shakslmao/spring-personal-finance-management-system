package com.devshaks.personal_finance.transactions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionsRepository extends JpaRepository<Transactions, Long> {
    List<Transactions> findByUserId(Long userId);
}
