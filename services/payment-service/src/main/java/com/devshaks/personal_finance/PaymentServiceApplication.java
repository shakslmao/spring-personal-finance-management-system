package com.devshaks.personal_finance;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@EnableFeignClients
@SpringBootApplication
public class PaymentServiceApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("POSTGRES_USER", dotenv.get("POSTGRES_USER"));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD"));
		System.setProperty("STRIPE_API_SECRET_KEY", dotenv.get("STRIPE_API_SECRET_KEY"));
		System.setProperty("STRIPE_API_PUBLISHABLE_KEY", dotenv.get("STRIPE_API_PUBLISHABLE_KEY"));
		SpringApplication.run(PaymentServiceApplication.class, args);
	}
}
