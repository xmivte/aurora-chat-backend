package com.example.kns.group.repository;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.group.model.Group;
import com.example.kns.user.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.TestInstance;

import javax.sql.DataSource;
import java.util.UUID;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestEmbeddedPostgresConfig.class)
@TestPropertySource("classpath:application-test.yml")
public class GroupRepositoryTest {

	@Autowired
	GroupRepository groupRepository;

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
	void findAllGroupsByUserId_WhenUserHasGroups_ReturnsGroups() throws Exception {

		List<Group> groups = groupRepository.findAllGroupsByUserId(userId);

		Group groupTest = new Group(groupId, "group", "avatar.png");

		assertThat(groups).hasSize(1);
		assertThat(groups.get(0)).isEqualTo(groupTest);
	}

	@Test
	void findAllGroupsByUserId_WhenNoGroups_ReturnsEmptyList() {
		String userIdNonExistent = UUID.randomUUID().toString();

		List<Group> groups = groupRepository.findAllGroupsByUserId(userIdNonExistent);

		assertThat(groups).isEmpty();
	}
}
