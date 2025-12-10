package com.example.kns.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials}")
    private Resource serviceAccountResource;

    @PostConstruct
    public void firebaseInitializer() {
        try{
            var credentials = GoogleCredentials.fromStream(serviceAccountResource.getInputStream());
            var options = FirebaseOptions.builder().setCredentials(credentials).build();

            FirebaseApp.initializeApp(options);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}