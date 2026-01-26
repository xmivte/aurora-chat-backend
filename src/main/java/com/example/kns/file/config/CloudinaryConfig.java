package com.example.kns.file.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Profile("!test")
@Configuration
public class CloudinaryConfig {

	@Bean
	@Profile("local")
	public Cloudinary cloudinaryLocal(@Value("${cloudinary.credentials}") Resource resource) throws IOException {
		try (var stream = resource.getInputStream()) {
			return initializeCloudinary(stream);
		}
	}

	@Bean
	@Profile("!local")
	public Cloudinary cloudinaryDeployed(@Value("${cloudinary.credentials}") String secretPath) throws IOException {
		try (var stream = new FileInputStream(secretPath)) {
			return initializeCloudinary(stream);
		}
	}

	private Cloudinary initializeCloudinary(InputStream credentialsStream) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> credentials = mapper.readValue(credentialsStream, new TypeReference<Map<String, String>>() {
		});

		log.info("Initializing Cloudinary with cloud_name: {}", credentials.get("cloud_name"));

		return new Cloudinary(ObjectUtils.asMap("cloud_name", credentials.get("cloud_name"), "api_key",
				credentials.get("api_key"), "api_secret", credentials.get("api_secret"), "secure", true));
	}
}
