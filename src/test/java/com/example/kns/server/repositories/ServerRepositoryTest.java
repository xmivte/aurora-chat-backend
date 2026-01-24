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

	String userId;
	Long serverId;
	String groupForServerId;
	Long serverGroupId;
	String serverName;
	String serverColor;

	String userId2;

	@Autowired
	private ServerRepository serverRepository;

	@Autowired
	private ServerGroupsRepository serverGroupsRepository;

	@Autowired
	private ServerGroupUserRepository serverGroupUserRepository;

	@BeforeAll
	void setUp() throws Exception {
		userId = UUID.randomUUID().toString();
		serverId = -1L;
		groupForServerId = UUID.randomUUID().toString();
		serverGroupId = -1L;
		serverName = "server";
		serverColor = "#FF004A";

		userId2 = UUID.randomUUID().toString();

		try (var conn = dataSource.getConnection()) {

			try (var userStmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				userStmt.setString(1, userId);
				userStmt.setString(2, "john");
				userStmt.setString(3, "jjohny@example.com");
				userStmt.setString(4, "avatar.png");
				userStmt.executeUpdate();
			}

			try (var userStmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				userStmt.setString(1, userId2);
				userStmt.setString(2, "john");
				userStmt.setString(3, "johnyy@example.com");
				userStmt.setString(4, "avatar.png");
				userStmt.executeUpdate();
			}

			try (var serverStmt = conn.prepareStatement(
					"INSERT INTO db.servers (id, name, user_id, background_Color_Hex) VALUES (?, ?, ?, ?)")) {
				serverStmt.setLong(1, serverId);
				serverStmt.setString(2, serverName);
				serverStmt.setString(3, userId);
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
					"INSERT INTO db.server_group_users (id, server_group_id, user_id) VALUES (?, ?, ?)")) {
				serverStmt.setLong(1, -1L);
				serverStmt.setLong(2, serverGroupId);
				serverStmt.setString(3, userId);
				serverStmt.executeUpdate();
			}

		}
	}

	@Test
	void findAllServersByUserId_WhenUserHasServers_ReturnsServers() {
		List<Server> servers = serverRepository.findAllServersByUserId(userId);

		Server expected = new Server(serverId, serverName, userId, serverColor);

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
		Server server = Server.builder().name(serverName).userId(userId2).build();

		serverRepository.insert(server);

		Long serverIdTemp = server.getId();
		String groupId = UUID.randomUUID().toString();
		ServerGroup serverGroup = ServerGroup.builder().serverId(serverIdTemp).groupId(groupId).build();

		groupRepository.insert(groupId, "main", null);
		serverGroupsRepository.insert(serverGroup);
		serverGroupUserRepository.insert(serverGroup.getId(), userId2);

		List<Server> servers = serverRepository.findAllServersByUserId(userId2);
		assertThat(servers).hasSize(1);
		server.setId(serverIdTemp);
		server.setBackgroundColorHex(servers.get(0).getBackgroundColorHex());

		assertThat(servers.get(0)).isEqualTo(server);
	}

	@Test
	void insertServer_WhenNoUser_ThrowsException() {
		String nonExistingUserId = UUID.randomUUID().toString();
		Server server = Server.builder().name("test-server").userId(nonExistingUserId).build();

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
		Server server = Server.builder().name(serverName).userId(userId2).build();

		serverRepository.insert(server);

		Long serverIdTemp = server.getId();
		String groupId = UUID.randomUUID().toString();
		ServerGroup serverGroup = ServerGroup.builder().serverId(serverIdTemp).groupId(groupId).build();

		groupRepository.insert(groupId, "main", null);
		serverGroupsRepository.insert(serverGroup);
		serverGroupUserRepository.insert(serverGroup.getId(), userId2);

		serverRepository.deleteServer(serverIdTemp);

		List<Server> servers = serverRepository.findAllServersByUserId(userId2);
		assertThat(servers).hasSize(0);

		assertThat(servers).isEmpty();
	}

	@Test
	void findServerGroupsWithUsers_WhenUserHasServerAndServerHasGroup_ReturnsFlattenedRows() {
		List<ServerGroupUserRow> rows = groupRepository.findServerGroupsWithUsers(userId);

		assertThat(rows).hasSize(1);
		ServerGroupUserRow row = rows.get(0);
		ServerGroupUserRow rowCopy = ServerGroupUserRow.builder().groupId(groupForServerId).groupName("main")
				.groupImage("").serverId(serverId).userId(userId).username("john").userImage("avatar.png").build();

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
		groupRepository.deleteServerGroups(serverGroupId);
		List<ServerGroupUserRow> rows = groupRepository.findServerGroupsWithUsers(userId);
		assertThat(rows).isEmpty();
	}
}
