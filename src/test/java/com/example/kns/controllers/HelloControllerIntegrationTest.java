package com.example.kns.controllers;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import(TestEmbeddedPostgresConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void sayHello_WithValidRequest_ReturnsGreetingMessage() throws Exception {
		mockMvc.perform(get("/hello").accept(MediaType.TEXT_PLAIN)).andExpect(status().isOk())
				.andExpect(content().string("Hello from Spring Boot!"));
	}

	@Test
	void greeting_WithValidRequest_ReturnsApproval() throws Exception {
		mockMvc.perform(get("/test").accept(MediaType.TEXT_PLAIN)).andExpect(status().isOk())
				.andExpect(content().string("approval"));
	}

	@Test
	void testSecuredEndpoint_noAuthorizationHeader_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/secure")).andExpect(status().isUnauthorized());
	}
}
