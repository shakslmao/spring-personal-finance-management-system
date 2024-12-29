package com.devshaks.personal_finance;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoAuditing
@EnableMongoRepositories
@SpringBootApplication
public class AuditServiceApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("MONGO_INITDB_ROOT_USERNAME", dotenv.get("MONGO_INITDB_ROOT_USERNAME"));
        System.setProperty("MONGO_INITDB_ROOT_PASSWORD", dotenv.get("MONGO_INITDB_ROOT_PASSWORD"));
        SpringApplication.run(AuditServiceApplication.class, args);
    }
}
