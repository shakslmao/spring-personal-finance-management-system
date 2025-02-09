package com.devshaks.personal_finance.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        serverHttpSecurity
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.addAllowedOriginPattern("http://localhost:3000");
                    configuration.addAllowedMethod("*");
                    configuration.addAllowedHeader("*");
                    configuration.setAllowCredentials(true);
                    configuration.addExposedHeader(HttpHeaders.SET_COOKIE);
                    return configuration;
                }))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/eureka/**").permitAll()
                        .pathMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/activate").permitAll()
                        .pathMatchers("/api/v1/**").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return serverHttpSecurity.build();
    }

    // add key here.
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256")).build();
    }
}
