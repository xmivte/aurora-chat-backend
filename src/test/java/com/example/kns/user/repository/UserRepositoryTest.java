package com.example.kns.user.repository;

import com.example.kns.config.TestEmbeddedPostgresConfig;
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
class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	DataSource dataSource;

	String userId;
	String groupId;

	@BeforeAll
	void setUp() throws Exception {
		userId = "userID1";
		groupId = "groupId1";

		try (var conn = dataSource.getConnection()) {

			try (var userStmt = conn.prepareStatement("""
					INSERT INTO db.users (id, username, email, image)
					VALUES (?, 'john', 'john@example.com', 'avatar.png')
					""")) {
				userStmt.setString(1, userId);
				userStmt.executeUpdate();
			}

			try (var groupStmt = conn.prepareStatement("""
					INSERT INTO db.groups (id, name, image)
					VALUES (?, 'group', 'avatar.png')
					""")) {
				groupStmt.setString(1, groupId);
				groupStmt.executeUpdate();
			}

			try (var userGroupStmt = conn.prepareStatement("""
					INSERT INTO db.user_groups (user_id, group_id)
					VALUES (?, ?)
					""")) {
				userGroupStmt.setString(1, userId);
				userGroupStmt.setString(2, groupId);
				userGroupStmt.executeUpdate();
			}
		}
	}

	@Test
	void findAllUsersByGroupId_WhenGroupHasUsers_ReturnsUsers() {
		List<User> users = userRepository.findAllUsersByGroupId(groupId);

		User expected = new User(userId, "john", "john@example.com", "avatar.png");

		assertThat(users).hasSize(1);
		assertThat(users.get(0)).isEqualTo(expected);
	}

	@Test
	void findAllUsersByGroupId_WhenGroupIsEmpty_ReturnsEmptyList() {
		String groupIdTest = UUID.randomUUID().toString();

		List<User> users = userRepository.findAllUsersByGroupId(groupIdTest);

		assertThat(users).isEmpty();
	}
}
