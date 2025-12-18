package com.example.kns.user.repository;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.user.model.User;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestEmbeddedPostgresConfig.class)
@TestPropertySource("classpath:application-test.yml")
class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	DataSource dataSource;

	@Test
	void findAllUsersByGroupId_WhenGroupHasUsers_ReturnsUsers() throws Exception {
		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {

			// insert user
			stmt.executeUpdate("""
					INSERT INTO db.users (id, username, email, image)
					VALUES ('%s', 'john', 'john@example.com', 'avatar.png')
					""".formatted(userId));

			stmt.executeUpdate("""
					INSERT INTO db.groups (id, name, image)
					VALUES ('%s', 'group', 'avatar.png')
					""".formatted(groupId));

			// insert group relation
			stmt.executeUpdate("""
					INSERT INTO db.user_groups (user_id, group_id)
					VALUES ('%s', '%s')
					""".formatted(userId, groupId));
		}

		List<User> users = userRepository.findAllUsersByGroupId(groupId);

		assertThat(users).hasSize(1);
		assertThat(users.get(0).getId()).isEqualTo(userId);
		assertThat(users.get(0).getUsername()).isEqualTo("john");
		assertThat(users.get(0).getEmail()).isEqualTo("john@example.com");
		assertThat(users.get(0).getImage()).isEqualTo("avatar.png");
	}

	@Test
	void findAllUsersByGroupId_WhenGroupIsEmpty_ReturnsEmptyList() {
		String groupId = UUID.randomUUID().toString();

		List<User> users = userRepository.findAllUsersByGroupId(groupId);

		assertThat(users).isEmpty();
	}

	@Test
	void checkEmbeddedDb_UsesEmbeddedPostgres() throws Exception {
		try (var conn = dataSource.getConnection()) {
			String url = conn.getMetaData().getURL();
			assertThat(url).doesNotContain("5455"); // not real DB
			assertThat(url).matches(".*:\\d{4,5}.*"); // random port
			assertThat(url).contains("localhost");
		}
	}
}
