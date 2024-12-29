package com.devshaks.personal_finance.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwtSetUri = "http://localhost:9098/realms/personal-finance/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwtSetUri).build();
    }
}
