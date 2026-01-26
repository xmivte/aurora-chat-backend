package com.example.kns.server_groups.repositories;

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
public class ServerGroupsRepositoryTest {

	@Autowired
	DataSource dataSource;

	String userId;
	Long serverId;
	String groupForServerId;
	String serverName;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	private ServerRepository serverRepository;

	@Autowired
	private ServerGroupsRepository serverGroupsRepository;

	@Autowired
	private ServerGroupUserRepository serverGroupUserRepository;

	@BeforeAll
	void setUp() throws Exception {
		userId = UUID.randomUUID().toString();
		serverId = 99L;
		groupForServerId = UUID.randomUUID().toString();
		serverName = "server";

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

			try (var userStmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				userStmt.setString(1, userId);
				userStmt.setString(2, "john");
				userStmt.setString(3, "kjohny@example.com");
				userStmt.setString(4, "avatar.png");
				userStmt.executeUpdate();
			}

			try (var serverStmt = conn
					.prepareStatement("INSERT INTO db.servers (id, name, user_id) VALUES (?, ?, ?)")) {
				serverStmt.setLong(1, serverId);
				serverStmt.setString(2, serverName);
				serverStmt.setString(3, userId);
				serverStmt.executeUpdate();
			}

			try (var serverStmt = conn.prepareStatement("INSERT INTO db.groups (id, name, image) VALUES (?, ?, ?)")) {
				serverStmt.setString(1, groupForServerId);
				serverStmt.setString(2, "main");
				serverStmt.setString(3, "");
				serverStmt.executeUpdate();
			}
		}
	}

	@Test
	void insertServerGroup_WhenUserExists_ReturnsInsertedServerGroup() {

		ServerGroup serverGroup = ServerGroup.builder().serverId(serverId).groupId(groupForServerId).build();
		serverGroupsRepository.insert(serverGroup);

		ServerGroup result = serverGroupsRepository.findByServerId(serverId);
		ServerGroup expected = ServerGroup.builder().id(result.getId()).serverId(serverId).groupId(groupForServerId)
				.build();

		assertThat(result).isEqualTo(expected);
	}

}
