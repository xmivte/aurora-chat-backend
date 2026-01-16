package com.example.kns.controllers;

import com.example.kns.dto.UserContext;
import com.example.kns.dto.UserDataDto;
import com.example.kns.services.UserAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAccountController.class)
public class UserAccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserAccountService userService;

	private UsernamePasswordAuthenticationToken mockAuth() {
		UserContext principal = new UserContext("userId5");
		return new UsernamePasswordAuthenticationToken(principal, null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
	}

	@Test
	void fetchUser_WithValidAuth_ReturnsUserData() throws Exception {
		UserDataDto mockUser = new UserDataDto("Matas#1234", "img_url");
		when(userService.getUser(any(UserContext.class))).thenReturn(mockUser);

		mockMvc.perform(get("/users").with(authentication(mockAuth())).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.username").value("Matas#1234"));
	}

	@Test
	void createUser_WithValidPayload_ReturnsCreatedUser() throws Exception {
		UserDataDto payload = new UserDataDto("NewUser", "img_url");
		when(userService.createUser(any(UserContext.class), any(UserDataDto.class))).thenReturn(payload);

		mockMvc.perform(post("/users").with(authentication(mockAuth())).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.username").value("NewUser"));
	}

	@Test
	void fetchUser_WithoutAuth_ReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
	}
}