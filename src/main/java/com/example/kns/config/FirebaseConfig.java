package com.example.kns.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Profile("!test")
@Configuration
public class FirebaseConfig {

	@Value("${firebase.credentials}")
	private Resource serviceAccountResource;

	@Bean
	public FirebaseApp firebaseInitializer() throws IOException {
		if (FirebaseApp.getApps().isEmpty()) {
			var credentials =
					GoogleCredentials.fromStream(serviceAccountResource.getInputStream());

			var options = FirebaseOptions.builder()
					.setCredentials(credentials)
					.build();

			return FirebaseApp.initializeApp(options);
		}
		return FirebaseApp.getInstance();
	}
}