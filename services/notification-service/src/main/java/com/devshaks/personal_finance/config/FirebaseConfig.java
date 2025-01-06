package com.devshaks.personal_finance.config;

import java.io.FileInputStream;

import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class FirebaseConfig {

    public FirebaseConfig() throws Exception {
        try {
            Dotenv dotenv = Dotenv.load();
            String firebaseCredentials = dotenv.get("FIREBASE_CREDENTIALS");

            if (firebaseCredentials == null || firebaseCredentials.isEmpty()) {
                throw new IllegalArgumentException("FIREBASE_CREDENTIALS environment variable is not set.");
            }

            FileInputStream serviceAccount = new FileInputStream(firebaseCredentials);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);

        } catch (Exception e) {
            throw new RuntimeException("Error Fetching FCM Configuration", e);
        }

    }
}
