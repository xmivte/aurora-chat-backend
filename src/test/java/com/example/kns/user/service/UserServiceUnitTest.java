package com.example.kns.user.service;

import com.example.kns.user.model.User;
import com.example.kns.user.repository.UserRepository;
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
class UserServiceUnitTest {

	@Mock
	private UserRepository mapper;

	@InjectMocks
	private UserService userService;

	@Test
	void getAll_WithValidGroupId_ReturnsUsers() {
		String groupId = "group-1";
		List<User> users = List.of(new User(), new User());

		when(mapper.findAllUsersByGroupId(groupId)).thenReturn(users);

		List<User> result = userService.getAll(groupId);

		assertThat(result).isEqualTo(users);
		verify(mapper).findAllUsersByGroupId(groupId);
	}

	@Test
	void getAll_WithBlankGroupId_ThrowsException() {
		String blankGroupId = " ";

		assertThatThrownBy(() -> userService.getAll(blankGroupId)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Group id is blank");
	}

	@Test
	void getAllUsers_ReturnsAllUsers() {
		List<User> users = List.of(new User(), new User());

		when(mapper.findAllUsers()).thenReturn(users);

		List<User> result = userService.getAllUsers();

		assertThat(result).isEqualTo(users);
		verify(mapper).findAllUsers();
	}
}
