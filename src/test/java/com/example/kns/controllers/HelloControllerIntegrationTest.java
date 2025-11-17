package com.example.kns.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
}
