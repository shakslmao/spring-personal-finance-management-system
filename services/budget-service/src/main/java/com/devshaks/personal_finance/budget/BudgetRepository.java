package com.devshaks.personal_finance.budget;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndMonth(Long userId, String month);
    List<Budget> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Budget b WHERE b.id = :id AND b.userId = :userId")
    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT COUNT (b) > 0 FROM Budget b WHERE b.id = :id AND b.userId = :userId")
    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);
}
