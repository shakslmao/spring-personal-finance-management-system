package com.devshaks.personal_finance.token;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface TokensRepository extends JpaRepository<Tokens, Long> {
    Optional<Tokens> findByToken(String token);
    Optional<Tokens> findByRefreshToken(String refreshToken);

    @Query("SELECT t FROM Tokens t WHERE t.user.id = :userId AND t.isRevoked = false AND t.expiresAt > CURRENT_TIMESTAMP")
    List<Tokens> findAllValidTokensByUser(Long userId);

    void deleteAllByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Tokens t SET t.isRevoked = true WHERE (t.token = :token OR t.refreshToken = :refreshToken) AND t.isRevoked = false")
    void revokeTokens(String token, String refreshToken);

    @Modifying
    @Transactional
    @Query("DELETE FROM Tokens t WHERE t.token = :token")
    void deleteByToken(String token);


    @Modifying
    @Transactional
    void deleteByRefreshToken(String refreshToken);
}
