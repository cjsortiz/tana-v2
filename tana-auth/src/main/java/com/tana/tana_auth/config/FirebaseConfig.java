package com.tana.tana_auth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.service-account-path:}")
    private String serviceAccountPath;

    @Value("${app.firebase.service-account-json:}")
    private String serviceAccountJson;

    @Bean
    @ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials credentials;

        try (InputStream inputStream = getCredentialStream()) {
            credentials = GoogleCredentials.fromStream(inputStream);
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build();

        FirebaseApp app = FirebaseApp.getApps().isEmpty()
            ? FirebaseApp.initializeApp(options)
            : FirebaseApp.getInstance();

        return FirebaseMessaging.getInstance(app);
    }

    private InputStream getCredentialStream() throws IOException {
        if (!ObjectUtils.isEmpty(serviceAccountJson)) {
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }

        if (!ObjectUtils.isEmpty(serviceAccountPath)) {
            return new FileInputStream(serviceAccountPath);
        }

        throw new IOException("Firebase is enabled, but no service account path or JSON was provided.");
    }
}
