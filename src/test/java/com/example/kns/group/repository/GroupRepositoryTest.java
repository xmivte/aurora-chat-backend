package com.example.kns.group.repository;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.group.model.Group;
<<<<<<< HEAD
import com.example.kns.group.dto.GroupUserRow;
import com.example.kns.user.model.User;
=======
>>>>>>> origin/main
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
public class GroupRepositoryTest {

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	DataSource dataSource;

	String userId;
	String groupId;

	@BeforeAll
	void setUp() throws Exception {
		userId = UUID.randomUUID().toString();
		groupId = UUID.randomUUID().toString();

		try (var conn = dataSource.getConnection()) {

			try (var userStmt = conn
					.prepareStatement("INSERT INTO db.users (id, username, email, image) VALUES (?, ?, ?, ?)")) {
				userStmt.setString(1, userId);
				userStmt.setString(2, "john");
				userStmt.setString(3, "johny@example.com");
				userStmt.setString(4, "avatar.png");
				userStmt.executeUpdate();
			}

			try (var groupStmt = conn.prepareStatement("INSERT INTO db.groups (id, name, image) VALUES (?, ?, ?)")) {
				groupStmt.setString(1, groupId);
				groupStmt.setString(2, "group");
				groupStmt.setString(3, "avatar.png");
				groupStmt.executeUpdate();
			}

			try (var userGroupStmt = conn
					.prepareStatement("INSERT INTO db.user_groups (user_id, group_id) VALUES (?, ?)")) {
				userGroupStmt.setString(1, userId);
				userGroupStmt.setString(2, groupId);
				userGroupStmt.executeUpdate();
			}
		}
	}

	@Test
	void findAllGroupsByUserId_WhenUserHasGroups_ReturnsGroups() {
		List<Group> groups = groupRepository.findAllGroupsByUserId(userId);

		Group expected = new Group(groupId, "group", "avatar.png");

		assertThat(groups).hasSize(1);
		assertThat(groups.get(0)).isEqualTo(expected);
	}

	@Test
	void findAllGroupsByUserId_WhenNoGroups_ReturnsEmptyList() {
		String userIdNonExistent = UUID.randomUUID().toString();

		List<Group> groups = groupRepository.findAllGroupsByUserId(userIdNonExistent);

		assertThat(groups).isEmpty();
	}

	@Test
	void findGroupsWithUsers_WhenUserHasGroups_ReturnsFlattenedRows()
	{
		List<GroupUserRow> rows = groupRepository.findGroupsWithUsers(userId);

		assertThat(rows).hasSize(1);

		GroupUserRow row = rows.get(0);

		assertThat(row.getGroupId()).isEqualTo(groupId);
		assertThat(row.getGroupName()).isEqualTo("group");
		assertThat(row.getGroupImage()).isEqualTo("avatar.png");
		assertThat(row.getUserId()).isEqualTo(userId);
		assertThat(row.getUserName()).isEqualTo("john");
		assertThat(row.getUserImage()).isEqualTo("avatar.png");
	}

	@Test
	void findGroupsWithUsers_WhenNoGroups_ReturnsEmptyList(){
		String nonExistingUser = UUID.randomUUID().toString();

		List<GroupUserRow> rows = groupRepository.findGroupsWithUsers(nonExistingUser);

		assertThat(rows).isEmpty();
	}

}
