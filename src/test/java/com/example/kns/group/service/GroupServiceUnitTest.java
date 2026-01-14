package com.example.kns.group.service;

import com.example.kns.group.model.Group;
import com.example.kns.user.model.User;
import com.example.kns.group.repository.GroupRepository;
import com.example.kns.user_groups.repository.UserGroupRepository;
import com.example.kns.group.dto.GroupUserRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupServiceUnitTest {

	@Mock
	private GroupRepository mapper;

	@Mock
	private UserGroupRepository userGroupRepository;

	@InjectMocks
	private GroupService groupService;

	@Test
	void getAll_WithValidUserId_ReturnsGroups() {
		String userId = "1";
		List<Group> groups = List.of(new Group(), new Group());

		when(mapper.findAllGroupsByUserId(userId)).thenReturn(groups);

		List<Group> result = groupService.getAll(userId);

		assertThat(result).isEqualTo(groups);
		verify(mapper).findAllGroupsByUserId(userId);
	}

	@Test
	void createGroup_InsertsGroupAndUserLinks() {
		String myUserId = "userA";
		String otherUserId = "userB";

		var result = groupService.createGroup(myUserId, otherUserId);

		assertThat(result.getId()).isNotBlank();
		assertThat(result.getName()).isEqualTo("Group Chat");
		assertThat(result.getImage()).isNull();

		verify(mapper).insert(result.getId(), "Group Chat", null);

		verify(userGroupRepository).insertMany(List.of(myUserId, otherUserId), result.getId());
	}

	@Test
	void getAllWithUsers_GroupRowsIntoNestedDTOs() {
		String userId = "1";

		List<GroupUserRow> rows = List.of(new GroupUserRow("g1", "Group 1", "img1", "u1", "Alice", "a.png"),
				new GroupUserRow("g1", "Group 1", "img1", "u2", "Bob", "p.png"),
				new GroupUserRow("g2", "Group 2", "img2", "u3", "Charlie", "c.png"));

		when(mapper.findGroupsWithUsers(userId)).thenReturn(rows);

		var result = groupService.getAllWithUsers(userId);
		assertThat(result).hasSize(2);

		var group1 = result.stream().filter(g -> g.getId().equals("g1")).findFirst().orElseThrow();
		assertThat(group1.getUsers()).hasSize(2);
		assertThat(group1.getUsers().get(0).getUsername()).isEqualTo("Alice");
		assertThat(group1.getUsers().get(1).getUsername()).isEqualTo("Bob");

		var group2 = result.stream().filter(g -> g.getId().equals("g2")).findFirst().orElseThrow();
		assertThat(group2.getUsers()).hasSize(1);
		assertThat(group2.getUsers().get(0).getUsername()).isEqualTo("Charlie");

		verify(mapper).findGroupsWithUsers(userId);
	}

}
