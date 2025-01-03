package com.devshaks.personal_finance.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("(/eureka/**)").permitAll()
                        .pathMatchers("/api/v1/users/register").permitAll()
                        .pathMatchers("/api/v1/users").authenticated()
                        .pathMatchers("/api/v1/budgets").authenticated()
                        .pathMatchers("/api/v1/transactions").authenticated()
                        .pathMatchers("/api/v1/users").authenticated()
                        .pathMatchers("/api/v1/payments").authenticated()
                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return serverHttpSecurity.build();
    }
}
