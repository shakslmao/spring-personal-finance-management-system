package com.devshaks.personal_finance.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionsRepository extends JpaRepository<Transactions, Long>, JpaSpecificationExecutor<Transactions> {
    List<Transactions> findByUserId(Long userId);
    List<Transactions> findByUserIdAndCategory(Long userId, String category);
    List<Transactions> findByUserIdAndTransactionDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
