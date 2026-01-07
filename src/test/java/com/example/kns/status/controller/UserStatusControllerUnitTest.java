package com.example.kns.status.controller;

import com.example.kns.status.UserStatusController;
import com.example.kns.status.UserStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserStatusControllerUnitTest {

	@Mock
	private UserStatusService userStatusService;

	@InjectMocks
	private UserStatusController userStatusController;

	@Test
	void ping_WithPrincipal_UpdatesUserPing() {
		Principal principal = mock(Principal.class);
		when(principal.getName()).thenReturn("userTEST");

		userStatusController.ping(principal);

		verify(userStatusService).updateUserPing("userTEST");
	}

	@Test
	void ping_WithNullPrincipal_DoesNothing() {
		userStatusController.ping(null);

		verify(userStatusService, times(0)).updateUserPing(any());
	}

	@Test
	void getOnlineUsers_ReturnsServiceResult() {
		Set<String> users = Set.of("user1", "user2");
		when(userStatusService.getOnlineUserIds()).thenReturn(users);

		Set<String> result = userStatusController.getOnlineUsers();

		assertThat(result).isEqualTo(users);
		verify(userStatusService).getOnlineUserIds();
	}
}
