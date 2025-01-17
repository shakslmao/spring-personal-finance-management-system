package com.devshaks.personal_finance.users.token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.qos.logback.core.subst.Token;

public interface TokensRepository extends JpaRepository<Token, Long> {
    Optional<Tokens> findByToken(String token);
}
