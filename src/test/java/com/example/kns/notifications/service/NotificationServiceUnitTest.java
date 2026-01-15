package com.example.kns.notifications.service;

import com.example.kns.notifications.NotificationService;
import com.example.kns.notifications.dto.NotificationEventDTO;
import com.example.kns.notifications.repository.UserGroupsRepository;
import com.example.kns.notifications.repository.model.UnreadCountRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

	@Mock
	private UserGroupsRepository userGroups;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	private NotificationService service;

	@Test
	void onNewMessageSaved_blankGroupId_doesNothing() {
		service.onNewMessageSaved("   ", "sender", 1L, "hi");
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void onNewMessageSaved_nullGroupId_doesNothing() {
		service.onNewMessageSaved(null, "sender", 1L, "hi");
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void onNewMessageSaved_blankSenderId_doesNothing() {
		service.onNewMessageSaved("g1", "   ", 1L, "hi");
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void onNewMessageSaved_nullSenderId_doesNothing() {
		service.onNewMessageSaved("g1", null, 1L, "hi");
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void onNewMessageSaved_nullMessageId_doesNothing() {
		service.onNewMessageSaved("g1", "u1", null, "hi");
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void onNewMessageSaved_incrementsUnreadAndSendsToRecipientsExceptSender() {
		String groupId = "g1";
		String senderId = "u1";
		Long messageId = 10L;

		UnreadCountRow r1 = mock(UnreadCountRow.class);
		when(r1.getUserId()).thenReturn("u2");
		when(r1.getUnreadCount()).thenReturn(3);

		UnreadCountRow r2 = mock(UnreadCountRow.class);
		when(r2.getUserId()).thenReturn("u3");
		when(r2.getUnreadCount()).thenReturn(1);

		when(userGroups.findUnreadCountsForGroupExceptSender(groupId, senderId)).thenReturn(List.of(r1, r2));

		service.onNewMessageSaved(groupId, senderId, messageId, "hello world");

		verify(userGroups).incrementUnreadForGroupExceptSender(groupId, senderId);
		verify(userGroups).findUnreadCountsForGroupExceptSender(groupId, senderId);

		ArgumentCaptor<String> uidCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<NotificationEventDTO> evtCaptor = ArgumentCaptor.forClass(NotificationEventDTO.class);

		verify(messagingTemplate, times(2)).convertAndSendToUser(uidCaptor.capture(), eq("/queue/notifications"),
				evtCaptor.capture());

		assertThat(uidCaptor.getAllValues()).containsExactlyInAnyOrder("u2", "u3");

		List<NotificationEventDTO> evts = evtCaptor.getAllValues();
		assertThat(evts).hasSize(2);
		assertThat(evts).allSatisfy(e -> {
			assertThat(e.getType()).isEqualTo("MESSAGE_CREATED");
			assertThat(e.getGroupId()).isEqualTo(groupId);
			assertThat(e.getFromUserId()).isEqualTo(senderId);
			assertThat(e.getMessageId()).isEqualTo(messageId);
			assertThat(e.getTimestamp()).isNotNull();
			assertThat(e.getContent()).isEqualTo("hello world");
		});

		assertThat(evts).anySatisfy(e -> assertThat(e.getUnreadCount()).isEqualTo(3));
		assertThat(evts).anySatisfy(e -> assertThat(e.getUnreadCount()).isEqualTo(1));
	}

	@Test
	void onNewMessageSaved_withTruncatedPreview_truncatesTo120Chars() {
		String groupId = "g1";
		String senderId = "u1";
		Long messageId = 10L;

		UnreadCountRow r1 = mock(UnreadCountRow.class);
		when(r1.getUserId()).thenReturn("u2");
		when(r1.getUnreadCount()).thenReturn(3);

		when(userGroups.findUnreadCountsForGroupExceptSender(groupId, senderId)).thenReturn(List.of(r1));

		String content = "x".repeat(200);
		service.onNewMessageSaved(groupId, senderId, messageId, content);

		ArgumentCaptor<NotificationEventDTO> evtCaptor = ArgumentCaptor.forClass(NotificationEventDTO.class);
		verify(messagingTemplate).convertAndSendToUser(eq("u2"), eq("/queue/notifications"), evtCaptor.capture());

		NotificationEventDTO evt = evtCaptor.getValue();
		assertThat(evt.getContent()).hasSize(120);
		assertThat(evt.getContent()).isEqualTo("x".repeat(120));
	}

	@Test
	void onNewMessageSaved_filtersOutInvalidRecipients() {
		String groupId = "g1";
		String senderId = "u1";
		Long messageId = 10L;

		UnreadCountRow valid = mock(UnreadCountRow.class);
		when(valid.getUserId()).thenReturn("u2");
		when(valid.getUnreadCount()).thenReturn(1);

		UnreadCountRow blank = mock(UnreadCountRow.class);
		when(blank.getUserId()).thenReturn("   ");

		UnreadCountRow senderRow = mock(UnreadCountRow.class);
		when(senderRow.getUserId()).thenReturn(senderId);

		UnreadCountRow nullUser = mock(UnreadCountRow.class);
		when(nullUser.getUserId()).thenReturn(null);

		when(userGroups.findUnreadCountsForGroupExceptSender(groupId, senderId))
				.thenReturn(Arrays.asList(valid, blank, null, senderRow, nullUser));

		service.onNewMessageSaved(groupId, senderId, messageId, "hello");

		verify(userGroups).incrementUnreadForGroupExceptSender(groupId, senderId);

		ArgumentCaptor<String> uidCaptor = ArgumentCaptor.forClass(String.class);
		verify(messagingTemplate, times(1)).convertAndSendToUser(uidCaptor.capture(), eq("/queue/notifications"),
				any(NotificationEventDTO.class));

		assertThat(uidCaptor.getAllValues()).containsExactly("u2");
	}

	@Test
	void onNewMessageSaved_noRecipients_doesNotSend() {
		when(userGroups.findUnreadCountsForGroupExceptSender("g1", "u1")).thenReturn(List.of());

		service.onNewMessageSaved("g1", "u1", 1L, "hi");

		verify(userGroups).incrementUnreadForGroupExceptSender("g1", "u1");
		verify(userGroups).findUnreadCountsForGroupExceptSender("g1", "u1");
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void onNewMessageSaved_nullRecipientsList_doesNotSend() {
		when(userGroups.findUnreadCountsForGroupExceptSender("g1", "u1")).thenReturn(null);

		service.onNewMessageSaved("g1", "u1", 1L, "hi");

		verify(userGroups).incrementUnreadForGroupExceptSender("g1", "u1");
		verify(userGroups).findUnreadCountsForGroupExceptSender("g1", "u1");
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void onNewMessageSaved_nullContent_sendsEmptyPreview() {
		String groupId = "g1";
		String senderId = "u1";
		Long messageId = 10L;

		UnreadCountRow r1 = mock(UnreadCountRow.class);
		when(r1.getUserId()).thenReturn("u2");
		when(r1.getUnreadCount()).thenReturn(1);

		when(userGroups.findUnreadCountsForGroupExceptSender(groupId, senderId)).thenReturn(List.of(r1));

		service.onNewMessageSaved(groupId, senderId, messageId, null);

		ArgumentCaptor<NotificationEventDTO> evtCaptor = ArgumentCaptor.forClass(NotificationEventDTO.class);
		verify(messagingTemplate).convertAndSendToUser(eq("u2"), eq("/queue/notifications"), evtCaptor.capture());

		NotificationEventDTO evt = evtCaptor.getValue();
		assertThat(evt.getContent()).isEmpty();
	}

	@Test
	void onNewMessageSaved_nullUnreadCount_defaultsToZero() {
		String groupId = "g1";
		String senderId = "u1";
		Long messageId = 10L;

		UnreadCountRow r1 = mock(UnreadCountRow.class);
		when(r1.getUserId()).thenReturn("u2");
		when(r1.getUnreadCount()).thenReturn(null);

		when(userGroups.findUnreadCountsForGroupExceptSender(groupId, senderId)).thenReturn(List.of(r1));

		service.onNewMessageSaved(groupId, senderId, messageId, "hi");

		ArgumentCaptor<NotificationEventDTO> evtCaptor = ArgumentCaptor.forClass(NotificationEventDTO.class);
		verify(messagingTemplate).convertAndSendToUser(eq("u2"), eq("/queue/notifications"), evtCaptor.capture());

		NotificationEventDTO evt = evtCaptor.getValue();
		assertThat(evt.getUnreadCount()).isEqualTo(0);
	}

	@Test
	void markRead_blankUserId_doesNothing() {
		service.markRead("   ", "g1");
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void markRead_nullUserId_doesNothing() {
		service.markRead(null, "g1");
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void markRead_blankGroupId_doesNothing() {
		service.markRead("u1", "   ");
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void markRead_nullGroupId_doesNothing() {
		service.markRead("u1", null);
		verifyNoInteractions(userGroups);
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	void markRead_marksReadAndSendsGroupReadEvent() {
		when(userGroups.findLatestMessageId("g1")).thenReturn(99L);

		service.markRead("u1", "g1");

		verify(userGroups).findLatestMessageId("g1");
		verify(userGroups).markGroupRead("u1", "g1", 99L);

		ArgumentCaptor<NotificationEventDTO> evtCaptor = ArgumentCaptor.forClass(NotificationEventDTO.class);
		verify(messagingTemplate).convertAndSendToUser(eq("u1"), eq("/queue/notifications"), evtCaptor.capture());

		NotificationEventDTO evt = evtCaptor.getValue();
		assertThat(evt.getType()).isEqualTo("GROUP_READ");
		assertThat(evt.getGroupId()).isEqualTo("g1");
		assertThat(evt.getFromUserId()).isNull();
		assertThat(evt.getMessageId()).isEqualTo(99L);
		assertThat(evt.getUnreadCount()).isEqualTo(0);
		assertThat(evt.getContent()).isNull();
		assertThat(evt.getTimestamp()).isNotNull();
	}

	@Test
	void markRead_withNullLatestMessageId_stillMarksRead() {
		when(userGroups.findLatestMessageId("g1")).thenReturn(null);

		service.markRead("u1", "g1");

		verify(userGroups).findLatestMessageId("g1");
		verify(userGroups).markGroupRead("u1", "g1", null);

		ArgumentCaptor<NotificationEventDTO> evtCaptor = ArgumentCaptor.forClass(NotificationEventDTO.class);
		verify(messagingTemplate).convertAndSendToUser(eq("u1"), eq("/queue/notifications"), evtCaptor.capture());

		NotificationEventDTO evt = evtCaptor.getValue();
		assertThat(evt.getMessageId()).isNull();
	}

}
