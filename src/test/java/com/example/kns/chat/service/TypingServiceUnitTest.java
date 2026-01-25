package com.example.kns.chat.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TypingServiceUnitTest {

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	private TypingService service;

	@Test
	void getTypingUsers_WithEmptyGroup_ReturnsEmptyGroupsSet() {
		String groupId = "1";
		Set<String> result = service.getTypingUserIds(groupId);
		assertThat(result).isEmpty();
	}

	@Test
	void updateUserTyping_WithGroupId_ReturnsNonEmptyGroupsSet() {
		String groupId = "group1";
		String user1 = "user1";
		String user2 = "user2";
		service.updateUserTyping(groupId, user1, true);
		service.updateUserTyping(groupId, user2, true);

		Set<String> result = service.getTypingUserIds(groupId);

		assertThat(result).isEqualTo(Set.of(user1, user2));
	}

	@Test
	void disconnectUser_WithGroup_ReturnsEmptyGroupsSet() {
		String groupId = "group1";
		String user1 = "user1";
		service.updateUserTyping(groupId, user1, true);

		service.disconnectUser(user1);
		Set<String> result = service.getTypingUserIds(groupId);

		assertThat(result).isEmpty();
	}

	@Test
	void broadcastTypingUsers_WithGroupId_ReturnsUserSet() {
		String groupId = "group1";
		String user1 = "user1";

		service.updateUserTyping(groupId, user1, true);

		verify(messagingTemplate).convertAndSend("/topic/typing-users/" + groupId, Set.of(user1));
	}
}
