package com.example.kns.notifications.controller;

import com.example.kns.config.TestEmbeddedPostgresConfig;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import(TestEmbeddedPostgresConfig.class)
@SpringBootTest(properties = {"spring.task.scheduling.enabled=false"})
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

	private String userId;
	private String otherUserId;
	private String groupId;
	private String anotherGroupId;
	private long lastMessageId;

	@BeforeEach
	void setUp() throws Exception {
		userId = "u-" + UUID.randomUUID();
		otherUserId = "u-" + UUID.randomUUID();
		groupId = "g-" + UUID.randomUUID();
		anotherGroupId = "g-" + UUID.randomUUID();

		String email1 = "u1+" + UUID.randomUUID() + "@example.com";
		String email2 = "u2+" + UUID.randomUUID() + "@example.com";

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.createStatement()) {
				stmt.executeUpdate("DELETE FROM db.chat_messages");
				stmt.executeUpdate("DELETE FROM db.user_groups");
				stmt.executeUpdate("DELETE FROM db.groups");
				stmt.executeUpdate("DELETE FROM db.users");
			}

			try (var stmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				stmt.setString(1, userId);
				stmt.setString(2, "u1");
				stmt.setString(3, email1);
				stmt.setString(4, null);
				stmt.executeUpdate();
			}
			try (var stmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				stmt.setString(1, otherUserId);
				stmt.setString(2, "u2");
				stmt.setString(3, email2);
				stmt.setString(4, null);
				stmt.executeUpdate();
			}

			try (var stmt = conn.prepareStatement("INSERT INTO db.groups (id, name, image) VALUES (?, ?, ?)")) {
				stmt.setString(1, groupId);
				stmt.setString(2, "g");
				stmt.setString(3, null);
				stmt.executeUpdate();
			}
			try (var stmt = conn.prepareStatement("INSERT INTO db.groups (id, name, image) VALUES (?, ?, ?)")) {
				stmt.setString(1, anotherGroupId);
				stmt.setString(2, "g2");
				stmt.setString(3, null);
				stmt.executeUpdate();
			}

			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId);
				stmt.setString(2, groupId);
				stmt.setInt(3, 2);
				stmt.executeUpdate();
			}
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId);
				stmt.setString(2, anotherGroupId);
				stmt.setInt(3, 5);
				stmt.executeUpdate();
			}
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, otherUserId);
				stmt.setString(2, groupId);
				stmt.setInt(3, 0);
				stmt.executeUpdate();
			}

			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.chat_messages (sender_id, group_id, content, sent) VALUES (?, ?, ?, ?) RETURNING id")) {
				stmt.setString(1, otherUserId);
				stmt.setString(2, groupId);
				stmt.setString(3, "hello");
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
		mockMvc.perform(
				get("/notifications/unread").with(jwt().jwt(j -> j.subject(userId))).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$." + groupId).value(2))
				.andExpect(jsonPath("$." + anotherGroupId).value(5));
	}

	@Test
	void getUnreadCounts_withNoUnreadMessages_returnsEmptyOrZeroMap() throws Exception {
		mockMvc.perform(get("/notifications/unread").with(jwt().jwt(j -> j.subject(otherUserId)))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$." + groupId).value(0));
	}

	@Test
	void getUnreadCounts_withoutAuth_returns401() throws Exception {
		mockMvc.perform(get("/notifications/unread").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void markRead_setsUnreadToZeroAndUpdatesLastReadMessageId() throws Exception {
		mockMvc.perform(post("/notifications/read").with(jwt().jwt(j -> j.subject(userId)))
				.contentType(MediaType.APPLICATION_JSON).content("{\"groupId\":\"" + groupId + "\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.groupId").value(groupId))
				.andExpect(jsonPath("$.unreadCount").value(0));

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"SELECT unread_count, last_read_message_id FROM db.user_groups WHERE user_id = ? AND group_id = ?")) {
				stmt.setString(1, userId);
				stmt.setString(2, groupId);
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
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
}
