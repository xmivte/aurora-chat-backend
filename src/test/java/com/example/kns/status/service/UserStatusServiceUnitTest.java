package com.example.kns.status.service;

import com.example.kns.status.UserStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.clearInvocations;

@ExtendWith(MockitoExtension.class)
public class UserStatusServiceUnitTest {

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	private UserStatusService userStatusService;

	@Test
	void updateUserPing_WhenNewUser_AddsUserAndBroadcasts() {
		String userId = "user1";

		userStatusService.updateUserPing(userId);

		assertThat(userStatusService.getOnlineUserIds()).contains(userId);
		verify(messagingTemplate).convertAndSend(eq("/topic/online-users"), any(Set.class));
	}

	@Test
	void updateUserPing_WhenExistingUser_UpdatesTimeButNoBroadcast() {
		String userId = "user1";

		userStatusService.updateUserPing(userId);
		clearInvocations(messagingTemplate);

		userStatusService.updateUserPing(userId);

		assertThat(userStatusService.getOnlineUserIds()).contains(userId);
		verify(messagingTemplate, times(0)).convertAndSend(eq("/topic/online-users"), any(Set.class));
	}

	@Test
	void getOnlineUserIds_ReturnsKeys() {
		userStatusService.updateUserPing("user1");
		userStatusService.updateUserPing("user2");

		Set<String> result = userStatusService.getOnlineUserIds();

		assertThat(result).containsExactlyInAnyOrder("user1", "user2");
	}

	@Test
	void broadcastOnlineUsers_SendsMessage() {
		userStatusService.broadcastOnlineUsers();
		verify(messagingTemplate).convertAndSend(eq("/topic/online-users"), any(Set.class));
	}
}
