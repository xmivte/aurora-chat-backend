package com.example.kns.group.repository;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.group.model.Group;
import com.example.kns.user.model.User;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.util.UUID;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestEmbeddedPostgresConfig.class)
@TestPropertySource("classpath:application-test.yml")
public class GroupRepositoryTest {

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	DataSource dataSource;

	@Test
	void findAllGroupsByUserId_WhenUserHasGroups_ReturnsGroups() throws Exception {
		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {

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

		List<Group> groups = groupRepository.findAllGroupsByUserId(userId);

		assertThat(groups).hasSize(1);
		assertThat(groups.get(0).getId()).isEqualTo(groupId);
		assertThat(groups.get(0).getName()).isEqualTo("group");
		assertThat(groups.get(0).getImage()).isEqualTo("avatar.png");
	}

	@Test
	void findAllGroupsByUserId_WhenNoGroups_ReturnsEmptyList() {
		String userId = UUID.randomUUID().toString();

		List<Group> groups = groupRepository.findAllGroupsByUserId(userId);

		assertThat(groups).isEmpty();
	}

	@Test
	void checkEmbeddedDb_UsesEmbeddedPostgres() throws Exception {
		try (var conn = dataSource.getConnection()) {
			String url = conn.getMetaData().getURL();
			assertThat(url).doesNotContain("5455");
			assertThat(url).matches(".*:\\d{4,5}.*");
			assertThat(url).contains("localhost");
		}
	}
}
