package com.example.kns.group.service;

import com.example.kns.group.model.Group;
import com.example.kns.user.model.User;
import com.example.kns.group.repository.GroupRepository;
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
	void getAll_WithBlankUserId_ThrowsException() {
		String blankUserId = " ";

		assertThatThrownBy(() -> groupService.getAll(blankUserId)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("User id is blank");
	}

	@Test
void createGroup_InsertsGroupAndUserLinks() {
    String myUserId = "userA";
    String otherUserId = "userB";

    var result = groupService.createGroup(myUserId, otherUserId);

    // Validate DTO
    assertThat(result.getId()).isNotBlank();
    assertThat(result.getName()).isEqualTo("GroupChat");
    assertThat(result.getImage()).isNull();

    // Validate repository calls
    verify(mapper).insert(
            result.getId(),
            "GroupChat",
            null
    );

    verify(userGroupRepository).insert(myUserId, result.getId());
    verify(userGroupRepository).insert(otherUserId, result.getId());
}

}
