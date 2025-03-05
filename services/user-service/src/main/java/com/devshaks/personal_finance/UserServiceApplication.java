package com.devshaks.personal_finance;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.crypto.SecretKey;
import java.util.Base64;

@Slf4j
@EnableAsync
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("/Users/shaks/Desktop/java/personal-finance-microservice/spring-personal-finance-management-system").load();
        System.setProperty("POSTGRES_USER", dotenv.get("POSTGRES_USER"));
        System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD"));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));

        SpringApplication.run(UserServiceApplication.class, args);
    }
}
