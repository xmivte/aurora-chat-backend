package com.example.kns.config;

import com.cloudinary.Cloudinary;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestCloudinaryConfig {

	@Bean
	public Cloudinary cloudinary() {
		return Mockito.mock(Cloudinary.class);
	}
}
