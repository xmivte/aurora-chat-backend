package com.example.kns.notifications.controller;

import com.example.kns.dto.UserContext;
import com.example.kns.notifications.NotificationService;

import com.example.kns.notifications.repository.UserGroupsRepository;

import com.example.kns.notifications.repository.model.UnreadCountRow;

import com.example.kns.user.model.User;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;

import java.util.List;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class NotificationsControllerUnitTest {

	@Mock
	private UserGroupsRepository userGroupsRepository;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private NotificationsController controller;

	@Test
	void getUnreadCounts_missingJwt_throws() {
		assertThatThrownBy(() -> controller.getUnreadCounts(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Missing authenticated user id");
	}

	@Test
	void getUnreadCounts_blankSubject_throws() {
		var userCtx = new UserContext("     ");

		assertThatThrownBy(() -> controller.getUnreadCounts(userCtx)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Missing authenticated user id");
	}

	@Test

	void getUnreadCounts_mapsRows_toMapAndDefaultsNullUnreadToZero() {
		var userCtx = new UserContext("temp@gmail.com");

		UnreadCountRow r1 = mock(UnreadCountRow.class);
		when(r1.getGroupId()).thenReturn("g1");
		when(r1.getUnreadCount()).thenReturn(2);

		UnreadCountRow r2 = mock(UnreadCountRow.class);
		when(r2.getGroupId()).thenReturn("g2");
		when(r2.getUnreadCount()).thenReturn(null);

		UnreadCountRow nullGroup = mock(UnreadCountRow.class);
		when(nullGroup.getGroupId()).thenReturn(null);
		when(userGroupsRepository.findUnreadCountsByUserId("temp@gmail.com"))
				.thenReturn(Arrays.asList(r1, r2, null, nullGroup));

		Map<String, Integer> out = controller.getUnreadCounts(userCtx);

		assertThat(out).containsEntry("g1", 2).containsEntry("g2", 0).hasSize(2);

		verify(userGroupsRepository).findUnreadCountsByUserId("temp@gmail.com");

	}

	@Test

	void getUnreadCounts_emptyList_returnsEmptyMap() {

		var userCtx = new UserContext("temp@gmail.com");
		when(userGroupsRepository.findUnreadCountsByUserId("temp@gmail.com")).thenReturn(List.of());

		Map<String, Integer> out = controller.getUnreadCounts(userCtx);
		assertThat(out).isEmpty();

	}

	@Test

	void markRead_nullJwt_throws() {

		var req = new NotificationsController.MarkReadRequest("g1");
		assertThatThrownBy(() -> controller.markRead(null, req)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Missing authenticated user id");

	}

	@Test

	void markRead_nullBody_throws() {

		var userCtx = new UserContext("temp@gmail.com");

		assertThatThrownBy(() -> controller.markRead(userCtx, null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Group id is blank");

	}

	@Test

	void markRead_blankGroupId_throws() {

		var userCtx = new UserContext("temp@gmail.com");
		var req = new NotificationsController.MarkReadRequest("   ");

		assertThatThrownBy(() -> controller.markRead(userCtx, req)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Group id is blank");
	}

	@Test

	void markRead_nullGroupId_throws() {

		var userCtx = new UserContext("temp@gmail.com");
		var req = new NotificationsController.MarkReadRequest(null);

		assertThatThrownBy(() -> controller.markRead(userCtx, req)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Group id is blank");
	}

	@Test

	void markRead_valid_callsServiceAndReturnsResponse() {

		var userCtx = new UserContext("temp@gmail.com");
		var req = new NotificationsController.MarkReadRequest("g1");

		Map<String, Object> out = controller.markRead(userCtx, req);

		verify(notificationService).markRead("temp@gmail.com", "g1");
		assertThat(out).containsEntry("groupId", "g1").containsEntry("unreadCount", 0);
	}
}