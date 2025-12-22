package com.example.kns.controllers;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import com.example.kns.config.TestEmbeddedPostgresConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import(TestEmbeddedPostgresConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestEmbeddedPostgresConfig.class)
public class HelloControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	FirebaseAuth firebaseAuth;

	@BeforeEach
	void setup() throws FirebaseAuthException {
		long iat = Instant.now().getEpochSecond();
		long exp = iat + 3600;

		FirebaseToken token = mock(FirebaseToken.class);
		when(token.getClaims()).thenReturn(Map.of("sub", "userId5", "iat", iat, "exp", exp));
		when(firebaseAuth.verifyIdToken(anyString())).thenReturn(token);
	}

	@Test
	void sayHello_WithValidRequest_ReturnsGreetingMessage() throws Exception {
		mockMvc.perform(get("/hello").header("Authorization", "Bearer fake-token").accept(MediaType.TEXT_PLAIN))
				.andExpect(status().isOk()).andExpect(content().string("Hello from Spring Boot!"));
	}

	@Test
	void greeting_WithValidRequest_ReturnsApproval() throws Exception {
		mockMvc.perform(get("/test").header("Authorization", "Bearer fake-token").accept(MediaType.TEXT_PLAIN))
				.andExpect(status().isOk()).andExpect(content().string("approval"));
	}

	@Test
	void testSecuredEndpoint_noAuthorizationHeader_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/secure")).andExpect(status().isUnauthorized());
	}
}
