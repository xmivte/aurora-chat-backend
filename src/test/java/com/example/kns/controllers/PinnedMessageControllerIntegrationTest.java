package com.example.kns.controllers;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import com.example.kns.group.repository.GroupRepository;
import com.example.kns.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import(TestEmbeddedPostgresConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
class PinnedMessageControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	UserRepository userRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	ChatMessagesRepository chatMessagesRepository;

	@MockBean
	FirebaseAuth firebaseAuth;

	@BeforeEach
	void setUp() throws FirebaseAuthException {
		userRepository.insert("userId5", "test-user", "userId5@test.com", null);
		groupRepository.insert("group-1", "group-1", null);

		long iat = Instant.now().getEpochSecond();
		long exp = iat + 3600;

		FirebaseToken token = mock(FirebaseToken.class);
		when(token.getClaims()).thenReturn(Map.of("sub", "userId5", "iat", iat, "exp", exp));
		when(firebaseAuth.verifyIdToken(anyString())).thenReturn(token);
	}

	@Test
	void pin_ThenList_ThenUnpin() throws Exception {
		String groupId = "group-1";

		ChatMessage msg = ChatMessage.builder().senderId("userId5").groupId(groupId).content("hello").sent(true)
				.build();
		chatMessagesRepository.insert(msg);
		Long messageId = msg.getId();

		mockMvc.perform(post("/api/groups/{groupId}/pinned-messages", groupId).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("""
							{"messageId": %d, "pinnedBy": "userId5"}
						""".formatted(messageId))).andExpect(status().isOk()).andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.groupId").value(groupId))
				.andExpect(jsonPath("$.messageId").value(messageId.intValue()))
				.andExpect(jsonPath("$.pinnedBy").value("userId5")).andExpect(jsonPath("$.pinnedAt").exists());

		mockMvc.perform(get("/api/groups/{groupId}/pinned-messages", groupId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].groupId").value(groupId))
				.andExpect(jsonPath("$[0].messageId").value(messageId.intValue()))
				.andExpect(jsonPath("$[0].pinnedBy").value("userId5")).andExpect(jsonPath("$[0].pinnedAt").exists());

		mockMvc.perform(delete("/api/groups/{groupId}/pinned-messages/{messageId}", groupId, messageId))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/groups/{groupId}/pinned-messages", groupId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}
}
