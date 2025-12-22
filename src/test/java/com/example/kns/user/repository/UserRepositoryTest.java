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
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestEmbeddedPostgresConfig.class)
@TestPropertySource("classpath:application-test.yml")
class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	DataSource dataSource;

	String userId;
	String groupId;

	@BeforeAll
	void setUp(@Autowired DataSource dataSource) throws Exception {
		userId = UUID.randomUUID().toString();
		groupId = UUID.randomUUID().toString();

		try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {

			stmt.executeUpdate("""
					    INSERT INTO db.users (id, username, email, image)
					    VALUES ('%s', 'john', 'john@example.com', 'avatar.png')
					""".formatted(userId));

			stmt.executeUpdate("""
					    INSERT INTO db.groups (id, name, image)
					    VALUES ('%s', 'group', 'avatar.png')
					""".formatted(groupId));

			stmt.executeUpdate("""
					    INSERT INTO db.user_groups (user_id, group_id)
					    VALUES ('%s', '%s')
					""".formatted(userId, groupId));
		}
	}

	@Test
	void findAllUsersByGroupId_WhenGroupHasUsers_ReturnsUsers() throws Exception {
		List<User> users = userRepository.findAllUsersByGroupId(groupId);

		User groupTest = new User(userId, "john", "john@example.com", "avatar.png");

		assertThat(users).hasSize(1);
		assertThat(users.get(0)).isEqualTo(groupTest);
	}

	@Test
	void findAllUsersByGroupId_WhenGroupIsEmpty_ReturnsEmptyList() {
		String groupId = UUID.randomUUID().toString();

		List<User> users = userRepository.findAllUsersByGroupId(groupId);

		assertThat(users).isEmpty();
	}
}
