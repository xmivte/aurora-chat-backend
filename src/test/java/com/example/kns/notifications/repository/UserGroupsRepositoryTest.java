package com.example.kns.notifications.repository;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.notifications.repository.model.UnreadCountRow;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(TestEmbeddedPostgresConfig.class)
@SpringBootTest(properties = {"spring.task.scheduling.enabled=false"})
class UserGroupsRepositoryTest {

	@Autowired
	private UserGroupsRepository repository;

	@Autowired
	private DataSource dataSource;

	@MockBean
	private FirebaseAuth firebaseAuth;

	private String userId1;
	private String userId2;
	private String groupId1;
	private String groupId2;

	@BeforeEach
	void setUp() throws Exception {
		userId1 = "u-" + UUID.randomUUID();
		userId2 = "u-" + UUID.randomUUID();
		groupId1 = "g-" + UUID.randomUUID();
		groupId2 = "g-" + UUID.randomUUID();

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.createStatement()) {
				stmt.executeUpdate("DELETE FROM db.chat_messages");
				stmt.executeUpdate("DELETE FROM db.user_groups");
				stmt.executeUpdate("DELETE FROM db.groups");
				stmt.executeUpdate("DELETE FROM db.users");
			}

			try (var stmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				stmt.setString(1, userId1);
				stmt.setString(2, "user1");
				stmt.setString(3, "user1@test.com");
				stmt.setString(4, null);
				stmt.executeUpdate();

				stmt.setString(1, userId2);
				stmt.setString(2, "user2");
				stmt.setString(3, "user2@test.com");
				stmt.setString(4, null);
				stmt.executeUpdate();
			}

			try (var stmt = conn.prepareStatement("INSERT INTO db.groups (id, name, image) VALUES (?, ?, ?)")) {
				stmt.setString(1, groupId1);
				stmt.setString(2, "group1");
				stmt.setString(3, null);
				stmt.executeUpdate();

				stmt.setString(1, groupId2);
				stmt.setString(2, "group2");
				stmt.setString(3, null);
				stmt.executeUpdate();
			}
		}
	}

	@Test
	void incrementUnreadForGroupExceptSender_incrementsAllMembersExceptSender() throws Exception {
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				stmt.setInt(3, 5);
				stmt.executeUpdate();

				stmt.setString(1, userId2);
				stmt.setString(2, groupId1);
				stmt.setInt(3, 0);
				stmt.executeUpdate();
			}
		}

		repository.incrementUnreadForGroupExceptSender(groupId1, userId1);

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn
					.prepareStatement("SELECT unread_count FROM db.user_groups WHERE user_id = ? AND group_id = ?")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				try (var rs = stmt.executeQuery()) {
					rs.next();
					assertThat(rs.getInt("unread_count")).isEqualTo(5);
				}

				stmt.setString(1, userId2);
				stmt.setString(2, groupId1);
				try (var rs = stmt.executeQuery()) {
					rs.next();
					assertThat(rs.getInt("unread_count")).isEqualTo(1);
				}
			}
		}
	}

	@Test
	void incrementUnreadForGroupExceptSender_handlesNullUnreadCount() throws Exception {
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId2);
				stmt.setString(2, groupId1);
				stmt.setNull(3, java.sql.Types.INTEGER);
				stmt.executeUpdate();
			}
		}

		repository.incrementUnreadForGroupExceptSender(groupId1, userId1);

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn
					.prepareStatement("SELECT unread_count FROM db.user_groups WHERE user_id = ? AND group_id = ?")) {
				stmt.setString(1, userId2);
				stmt.setString(2, groupId1);
				try (var rs = stmt.executeQuery()) {
					rs.next();
					assertThat(rs.getInt("unread_count")).isEqualTo(1);
				}
			}
		}
	}

	@Test
	void findUnreadCountsByUserId_returnsAllGroupsForUser() throws Exception {
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				stmt.setInt(3, 3);
				stmt.executeUpdate();

				stmt.setString(1, userId1);
				stmt.setString(2, groupId2);
				stmt.setInt(3, 7);
				stmt.executeUpdate();

				stmt.setString(1, userId2);
				stmt.setString(2, groupId1);
				stmt.setInt(3, 0);
				stmt.executeUpdate();
			}
		}

		List<UnreadCountRow> rows = repository.findUnreadCountsByUserId(userId1);

		assertThat(rows).hasSize(2);
		assertThat(rows).anySatisfy(r -> {
			assertThat(r.getGroupId()).isEqualTo(groupId1);
			assertThat(r.getUnreadCount()).isEqualTo(3);
		});
		assertThat(rows).anySatisfy(r -> {
			assertThat(r.getGroupId()).isEqualTo(groupId2);
			assertThat(r.getUnreadCount()).isEqualTo(7);
		});
	}

	@Test
	void findUnreadCountsByUserId_returnsEmptyListWhenNoGroups() {
		List<UnreadCountRow> rows = repository.findUnreadCountsByUserId(userId1);

		assertThat(rows).isEmpty();
	}

	@Test
	void findUnreadCountsByUserId_defaultsNullUnreadCountToZero() throws Exception {
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				stmt.setNull(3, java.sql.Types.INTEGER);
				stmt.executeUpdate();
			}
		}

		List<UnreadCountRow> rows = repository.findUnreadCountsByUserId(userId1);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getUnreadCount()).isEqualTo(0);
	}

	@Test
	void findUnreadCountsForGroupExceptSender_returnsAllMembersExceptSender() throws Exception {
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				stmt.setInt(3, 5);
				stmt.executeUpdate();

				stmt.setString(1, userId2);
				stmt.setString(2, groupId1);
				stmt.setInt(3, 2);
				stmt.executeUpdate();
			}
		}

		List<UnreadCountRow> rows = repository.findUnreadCountsForGroupExceptSender(groupId1, userId1);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getUserId()).isEqualTo(userId2);
		assertThat(rows.get(0).getUnreadCount()).isEqualTo(2);
	}

	@Test
	void findLatestMessageId_returnsMaxId() throws Exception {
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.chat_messages (sender_id, group_id, content, sent) VALUES (?, ?, ?, ?)")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				stmt.setString(3, "msg1");
				stmt.setBoolean(4, true);
				stmt.executeUpdate();

				stmt.setString(1, userId2);
				stmt.setString(2, groupId1);
				stmt.setString(3, "msg2");
				stmt.setBoolean(4, true);
				stmt.executeUpdate();
			}
		}

		Long maxId = repository.findLatestMessageId(groupId1);

		assertThat(maxId).isNotNull();
		assertThat(maxId).isPositive();
	}

	@Test
	void findLatestMessageId_returnsNullWhenNoMessages() {
		Long maxId = repository.findLatestMessageId(groupId1);

		assertThat(maxId).isNull();
	}

	@Test
	void markGroupRead_updatesUnreadCountAndLastReadMessageId() throws Exception {
		long messageId;
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.chat_messages (sender_id, group_id, content, sent) VALUES (?, ?, ?, ?) RETURNING id")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				stmt.setString(3, "test message");
				stmt.setBoolean(4, true);
				try (var rs = stmt.executeQuery()) {
					rs.next();
					messageId = rs.getLong(1);
				}
			}

			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				stmt.setInt(3, 10);
				stmt.executeUpdate();
			}
		}

		repository.markGroupRead(userId1, groupId1, messageId);

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"SELECT unread_count, last_read_message_id FROM db.user_groups WHERE user_id = ? AND group_id = ?")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				try (var rs = stmt.executeQuery()) {
					rs.next();
					assertThat(rs.getInt("unread_count")).isEqualTo(0);
					assertThat(rs.getLong("last_read_message_id")).isEqualTo(messageId);
				}
			}
		}
	}

	@Test
	void markGroupRead_handlesNullMessageId() throws Exception {
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"INSERT INTO db.user_groups (user_id, group_id, unread_count) VALUES (?, ?, ?)")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				stmt.setInt(3, 5);
				stmt.executeUpdate();
			}
		}

		repository.markGroupRead(userId1, groupId1, null);

		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.prepareStatement(
					"SELECT unread_count, last_read_message_id FROM db.user_groups WHERE user_id = ? AND group_id = ?")) {
				stmt.setString(1, userId1);
				stmt.setString(2, groupId1);
				try (var rs = stmt.executeQuery()) {
					rs.next();
					assertThat(rs.getInt("unread_count")).isEqualTo(0);
					rs.getLong("last_read_message_id");
					assertThat(rs.wasNull()).isTrue();
				}
			}
		}
	}
}
