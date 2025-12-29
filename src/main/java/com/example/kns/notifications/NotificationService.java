package com.example.kns.notifications;

import com.example.kns.notifications.dto.NotificationEventDTO;
import com.example.kns.notifications.repository.UserGroupsRepository;
import com.example.kns.notifications.repository.model.UnreadCountRow;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private static final int PREVIEW_MAX_LEN = 120;

	private final UserGroupsRepository userGroups;

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean; SimpMessagingTemplate is managed by Spring and not exposed to untrusted code")
	private final SimpMessagingTemplate messagingTemplate;

	@Transactional
	public void onNewMessageSaved(String groupId, String senderId, Long messageId, String content) {
		if (groupId == null || groupId.isBlank()) {
			log.debug("NotificationService.onNewMessageSaved ignored: groupId is blank");
			return;
		}
		if (senderId == null || senderId.isBlank()) {
			log.debug("NotificationService.onNewMessageSaved ignored: senderId is blank (groupId={})", groupId);
			return;
		}
		if (messageId == null) {
			log.debug("NotificationService.onNewMessageSaved ignored: messageId is null (groupId={}, senderId={})",
					groupId, senderId);
			return;
		}

		userGroups.incrementUnreadForGroupExceptSender(groupId, senderId);

		String safe = (content == null) ? "" : content;
		String preview = safe.length() > PREVIEW_MAX_LEN ? safe.substring(0, PREVIEW_MAX_LEN) : safe;

		List<UnreadCountRow> recipients = userGroups.findUnreadCountsForGroupExceptSender(groupId, senderId);
		Instant now = Instant.now();

		if (recipients == null || recipients.isEmpty()) {
			log.debug("No notification recipients found (groupId={}, senderId={})", groupId, senderId);
			return;
		}

		for (UnreadCountRow row : recipients) {
			if (row == null) {
				continue;
			}

			String uid = row.getUserId();
			if (uid == null || uid.isBlank() || uid.equals(senderId)) {
				continue;
			}

			Integer rowUnread = row.getUnreadCount();
			int unreadCount = (rowUnread == null) ? 0 : rowUnread;

			NotificationEventDTO evt = NotificationEventDTO.builder().type("MESSAGE_CREATED").groupId(groupId)
					.fromUserId(senderId).messageId(messageId).content(preview).timestamp(now).unreadCount(unreadCount)
					.build();

			messagingTemplate.convertAndSendToUser(uid, "/queue/notifications", evt);
		}
	}

	@Transactional
	public void markRead(String userId, String groupId) {
		if (userId == null || userId.isBlank()) {
			log.debug("NotificationService.markRead ignored: userId is blank");
			return;
		}
		if (groupId == null || groupId.isBlank()) {
			log.debug("NotificationService.markRead ignored: groupId is blank (userId={})", userId);
			return;
		}

		Long lastMessageId = userGroups.findLatestMessageId(groupId);

		userGroups.markGroupRead(userId, groupId, lastMessageId);

		NotificationEventDTO evt = NotificationEventDTO.builder().type("GROUP_READ").groupId(groupId).fromUserId(null)
				.messageId(lastMessageId).content(null).timestamp(Instant.now()).unreadCount(0).build();

		messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", evt);
	}
}
