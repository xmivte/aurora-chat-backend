package com.example.kns.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Profile("!test")
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseConfig {
	@Bean
	@Profile("local")
	public FirebaseApp firebaseAppLocal(@Value("${firebase.credentials}") Resource resource) throws IOException {
		try (var stream = resource.getInputStream()) {
			return initializeFirebase(stream);
		}
	}

	@Bean
	@Profile("!local")
	public FirebaseApp firebaseAppDeployed(@Value("${firebase.credentials}") String secretPath) throws IOException {
		try (var stream = new FileInputStream(secretPath)) {
			return initializeFirebase(stream);
		}
	}

	private FirebaseApp initializeFirebase(InputStream serviceAccount) throws IOException {
		FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.build();
		return FirebaseApp.initializeApp(options);
	}

}