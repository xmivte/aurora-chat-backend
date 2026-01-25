package com.example.kns.notifications.controller;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.dto.UserContext;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import(TestEmbeddedPostgresConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
class NotificationsControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DataSource dataSource;

	@MockBean
	private SimpMessagingTemplate messagingTemplate;

	@MockBean
	private com.example.kns.chat.service.ChatMessagePoller chatMessagePoller;

	@MockBean
	private FirebaseAuth firebaseAuth;

	private String userEmail;
	private String otherUserEmail;
	private String groupId;
	private String anotherGroupId;
	private long lastMessageId;

	@BeforeEach
	void setUp() throws Exception {
		groupId = "g-" + UUID.randomUUID();
		anotherGroupId = "g-" + UUID.randomUUID();

		// These emails act as the IDs in the database
		userEmail = "u1+" + UUID.randomUUID() + "@example.com";
		otherUserEmail = "u2+" + UUID.randomUUID() + "@example.com";

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.createStatement()) {
				stmt.executeUpdate("DELETE FROM db.chat_messages");
				stmt.executeUpdate("DELETE FROM db.user_groups");
				stmt.executeUpdate("DELETE FROM db.groups");
				stmt.executeUpdate("DELETE FROM db.users");
			}

			// 1. Insert Users using explicit String IDs (Emails)
			// Schema change: id is now VARCHAR(100)
			try (var stmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				// User 1
				stmt.setString(1, userEmail);
				stmt.setString(2, "user1");
				stmt.setString(3, userEmail);
				stmt.setString(4, null);
				stmt.executeUpdate();

				// User 2
				stmt.setString(1, otherUserEmail);
				stmt.setString(2, "user2");
				stmt.setString(3, otherUserEmail);
				stmt.setString(4, null);
				stmt.executeUpdate();
			}

			// 2. Insert Groups (ID is VARCHAR)
			try (var stmt = conn.prepareStatement("INSERT INTO db.groups (id, name, image) VALUES (?, ?, ?)")) {
				stmt.setString(1, groupId);
				stmt.setString(2, "Group 1");
				stmt.setString(3, null);
				stmt.executeUpdate();

				stmt.setString(1, anotherGroupId);
				stmt.setString(2, "Group 2");
				stmt.setString(3, null);
				stmt.executeUpdate();
			}

			// 3. Insert User-Group relations
			// Schema change: user_id is now VARCHAR(100). We use the email (which is the
			// PK).
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				// Main user in Group 1
				stmt.setString(1, userEmail);
				stmt.setString(2, groupId);
				stmt.setInt(3, 2);
				stmt.executeUpdate();

				// Main user in Group 2
				stmt.setString(1, userEmail);
				stmt.setString(2, anotherGroupId);
				stmt.setInt(3, 5);
				stmt.executeUpdate();

				// Other user in Group 1
				stmt.setString(1, otherUserEmail);
				stmt.setString(2, groupId);
				stmt.setInt(3, 0);
				stmt.executeUpdate();
			}

			// 4. Insert a message
			// Schema change: sender_id is now VARCHAR(100). We use the email.
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.chat_messages (sender_id, group_id, content, sent) VALUES (?, ?, ?, ?) RETURNING id")) {
				stmt.setString(1, otherUserEmail);
				stmt.setString(2, groupId);
				stmt.setString(3, "test message");
				stmt.setBoolean(4, true);
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
					lastMessageId = rs.getLong(1);
				}
			}
		}
	}

	@Test
	void getUnreadCounts_returnsUnreadMapForAllGroups() throws Exception {
		mockMvc.perform(get("/notifications/unread").with(authentication(mockAuth(userEmail))) // Use UserContext
																								// principal
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.['" + groupId + "']").value(2))
				.andExpect(jsonPath("$.['" + anotherGroupId + "']").value(5));
	}

	@Test
	void getUnreadCounts_withNoUnreadMessages_returnsZeroInMap() throws Exception {
		mockMvc.perform(get("/notifications/unread").with(authentication(mockAuth(otherUserEmail))) // Use UserContext
																									// principal
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.['" + groupId + "']").value(0));
	}

	@Test
	void getUnreadCounts_withoutAuth_returns401() throws Exception {
		mockMvc.perform(get("/notifications/unread").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void markRead_setsUnreadToZeroAndUpdatesLastReadMessageId() throws Exception {
		mockMvc.perform(post("/notifications/read").with(authentication(mockAuth(userEmail))) // Use UserContext
																								// principal
				.contentType(MediaType.APPLICATION_JSON).content("{\"groupId\":\"" + groupId + "\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.groupId").value(groupId))
				.andExpect(jsonPath("$.unreadCount").value(0));

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"SELECT unread_count, last_read_message_id FROM db.user_groups WHERE user_id = ? AND group_id = ?")) {
				stmt.setString(1, userEmail); // Verification uses the String ID (email)
				stmt.setString(2, groupId);
				try (ResultSet rs = stmt.executeQuery()) {
					assertThat(rs.next()).isTrue();
					assertThat(rs.getInt("unread_count")).isEqualTo(0);
					assertThat(rs.getLong("last_read_message_id")).isEqualTo(lastMessageId);
				}
			}
		}
	}

	@Test
	void markRead_withoutAuth_returns401() throws Exception {
		mockMvc.perform(post("/notifications/read").contentType(MediaType.APPLICATION_JSON)
				.content("{\"groupId\":\"" + groupId + "\"}")).andExpect(status().isUnauthorized());
	}

	private Authentication mockAuth(String email) {
		UserContext userContext = new UserContext(email);
		return new UsernamePasswordAuthenticationToken(userContext, null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
	}
}
