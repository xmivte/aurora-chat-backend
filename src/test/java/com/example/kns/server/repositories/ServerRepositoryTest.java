package com.example.kns.server.repositories;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.group.dto.ServerGroupUserRow;
import com.example.kns.group.model.Group;
import com.example.kns.group.dto.GroupUserRow;
import com.example.kns.group.repository.GroupRepository;
import com.example.kns.server.model.Server;
import com.example.kns.server.repository.ServerRepository;
import com.example.kns.server_group_users.repository.ServerGroupUserRepository;
import com.example.kns.server_groups.model.ServerGroup;
import com.example.kns.server_groups.repository.ServerGroupsRepository;
import com.example.kns.user.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestEmbeddedPostgresConfig.class)
@TestPropertySource("classpath:application-test.yml")
@Transactional
@Rollback
public class ServerRepositoryTest {

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	DataSource dataSource;

	Long serverId;
	String groupForServerId;
	Long serverGroupId;
	String serverName;
	String serverColor;

	Long serverIdDelete;
	String userEmail1;
	String userEmail2;
	String userEmail3;
	String userId1;
	String userId2;
	String userId3;

	@Autowired
	private ServerRepository serverRepository;

	@Autowired
	private ServerGroupsRepository serverGroupsRepository;

	@Autowired
	private ServerGroupUserRepository serverGroupUserRepository;

	@BeforeAll
	void setUp() throws Exception {
		userEmail1 = UUID.randomUUID().toString();
		serverId = 1L;
		groupForServerId = UUID.randomUUID().toString();
		serverGroupId = 333L;
		serverName = "server";
		serverColor = "#FF004A";

		serverIdDelete = 50L;

		userEmail1 = UUID.randomUUID().toString();
		userEmail2 = UUID.randomUUID().toString();
		userEmail3 = UUID.randomUUID().toString();

		userId1 = UUID.randomUUID().toString();
		userId2 = UUID.randomUUID().toString();
		userId3 = UUID.randomUUID().toString();

		try (var conn = dataSource.getConnection()) {
			try (var cleanup = conn.prepareStatement("""
					    TRUNCATE TABLE
					        db.server_group_users,
					        db.server_groups,
					        db.servers
					    RESTART IDENTITY CASCADE
					""")) {
				cleanup.execute();
			}

			try (var stmt = conn.prepareStatement(
					"SELECT setval('db.server_groups_id_seq', (SELECT MAX(id) FROM db.server_groups))")) {
				stmt.execute();
			}

			try (var userStmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				userStmt.setString(1, userId1);
				userStmt.setString(2, "john");
				userStmt.setString(3, userEmail1);
				userStmt.setString(4, "avatar.png");
				userStmt.executeUpdate();
			}

			try (var userStmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				userStmt.setString(1, userId2);
				userStmt.setString(2, "john");
				userStmt.setString(3, userEmail2);
				userStmt.setString(4, "avatar.png");
				userStmt.executeUpdate();
			}

			try (var userStmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				userStmt.setString(1, userId3);
				userStmt.setString(2, "john");
				userStmt.setString(3, userEmail3);
				userStmt.setString(4, "avatar.png");
				userStmt.executeUpdate();
			}

			try (var serverStmt = conn.prepareStatement(
					"INSERT INTO db.servers (id, name, user_email, background_Color_Hex) VALUES (?, ?, ?, ?)")) {
				serverStmt.setLong(1, serverId);
				serverStmt.setString(2, serverName);
				serverStmt.setString(3, userEmail1);
				serverStmt.setString(4, serverColor);
				serverStmt.executeUpdate();
			}

			try (var serverStmt = conn.prepareStatement(
					"INSERT INTO db.servers (id, name, user_email, background_Color_Hex) VALUES (?, ?, ?, ?)")) {
				serverStmt.setLong(1, serverIdDelete);
				serverStmt.setString(2, serverName);
				serverStmt.setString(3, userEmail3);
				serverStmt.setString(4, serverColor);
				serverStmt.executeUpdate();
			}

			try (var serverStmt = conn.prepareStatement("INSERT INTO db.groups (id, name, image) VALUES (?, ?, ?)")) {
				serverStmt.setString(1, groupForServerId);
				serverStmt.setString(2, "main");
				serverStmt.setString(3, "");
				serverStmt.executeUpdate();
			}

			try (var serverStmt = conn
					.prepareStatement("INSERT INTO db.server_groups (id, server_id, group_id) VALUES (?, ?, ?)")) {
				serverStmt.setLong(1, serverGroupId);
				serverStmt.setLong(2, serverId);
				serverStmt.setString(3, groupForServerId);
				serverStmt.executeUpdate();
			}

			try (var serverStmt = conn.prepareStatement(
					"INSERT INTO db.server_group_users (id, server_group_id, user_email) VALUES (?, ?, ?)")) {
				serverStmt.setLong(1, -1L);
				serverStmt.setLong(2, serverGroupId);
				serverStmt.setString(3, userEmail1);
				serverStmt.executeUpdate();
			}

		}
	}

	@Test
	void findAllServersByUserId_WhenUserHasServers_ReturnsServers() {
		List<Server> servers = serverRepository.findAllServersByUserId(userEmail1);

		Server expected = new Server(serverId, serverName, userEmail1, serverColor);

		assertThat(servers).hasSize(1);
		assertThat(servers.get(0)).isEqualTo(expected);
	}

	@Test
	void findAllServersByUserId_WhenNoServers_ReturnsEmptyList() {
		String userIdNonExistent = UUID.randomUUID().toString();

		List<Server> servers = serverRepository.findAllServersByUserId(userIdNonExistent);

		assertThat(servers).isEmpty();
	}

	@Test
	void insertServer_WhenUserExists_ReturnsInsertedServer() {
		Server server = Server.builder().name(serverName).userEmail(userEmail2).build();

		serverRepository.insert(server);

		Long serverIdTemp = server.getId();
		String groupId = UUID.randomUUID().toString();
		ServerGroup serverGroup = ServerGroup.builder().serverId(serverIdTemp).groupId(groupId).build();

		groupRepository.insert(groupId, "main", null);
		serverGroupsRepository.insert(serverGroup);
		serverGroupUserRepository.insert(serverGroup.getId(), userEmail2);

		List<Server> servers = serverRepository.findAllServersByUserId(userEmail2);
		assertThat(servers).hasSize(1);
		server.setId(serverIdTemp);
		server.setBackgroundColorHex(servers.get(0).getBackgroundColorHex());

		assertThat(servers.get(0)).isEqualTo(server);
	}

	@Test
	void insertServer_WhenNoUser_ThrowsException() {
		String nonExistingUserEmail = UUID.randomUUID().toString();
		Server server = Server.builder().name("test-server").userEmail(nonExistingUserEmail).build();

		assertThatThrownBy(() -> serverRepository.insert(server)).isInstanceOf(RuntimeException.class);
	}

	@Test
	void findGroupsWithUsers_WhenNoGroups_ReturnsEmptyList() {
		String nonExistingUser = UUID.randomUUID().toString();

		List<GroupUserRow> rows = groupRepository.findGroupsWithUsers(nonExistingUser);

		assertThat(rows).isEmpty();
	}

	@Test
	void deleteServer_WhenUserExists_ReturnsEmptyList() {
		serverRepository.deleteServer(serverIdDelete, userEmail3);

		List<Server> servers = serverRepository.findAllServersByUserId(userEmail3);
		assertThat(servers).hasSize(0);

		assertThat(servers).isEmpty();
	}

	@Test
	void findServerGroupsWithUsers_WhenUserHasServerAndServerHasGroup_ReturnsFlattenedRows() {
		List<ServerGroupUserRow> rows = groupRepository.findServerGroupsWithUsers(userEmail1);

		assertThat(rows).hasSize(1);
		ServerGroupUserRow row = rows.get(0);
		ServerGroupUserRow rowCopy = ServerGroupUserRow.builder().groupId(groupForServerId).groupName("main")
				.groupImage("").serverId(serverId).userId(userId1).userEmail(userEmail1).username("john")
				.userImage("avatar.png").build();

		assertThat(row).isEqualTo(rowCopy);
	}

	@Test
	void findServerGroupsWithUsers_WhenNoServer_ReturnsEmptyList() {
		String nonExistingUser = UUID.randomUUID().toString();

		List<ServerGroupUserRow> rows = groupRepository.findServerGroupsWithUsers(nonExistingUser);

		assertThat(rows).isEmpty();
	}

	@Test
	void deleteSeverGroup_WhenServerHasGroup_ReturnsEmptyList() {
		serverGroupUserRepository.deleteServerGroupUsers(serverId, userEmail1);
		serverGroupsRepository.deleteServerGroups(serverId, userEmail1);
		groupRepository.deleteServerGroups(serverId, userEmail1);
		List<ServerGroupUserRow> rows = groupRepository.findServerGroupsWithUsers(userEmail1);
		assertThat(rows).isEmpty();
	}
}
