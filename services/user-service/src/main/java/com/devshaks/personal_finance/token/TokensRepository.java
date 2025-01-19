package com.devshaks.personal_finance.token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.qos.logback.core.subst.Token;

public interface TokensRepository extends JpaRepository<Tokens, Long> {
    Optional<Tokens> findByToken(String token);
}
