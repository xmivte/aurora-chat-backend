package com.example.kns.pin_message.service;

import com.example.kns.pin_message.model.PinnedMessage;
import com.example.kns.pin_message.repository.PinnedMessageRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI beans")
public class PinnedMessageService {

	private final PinnedMessageRepository pinnedRepo;
	private final SimpMessagingTemplate messagingTemplate;

	private static final int MAX_PIN_COUNT = 100;

	public PinnedMessage pinMessage(@NotBlank(message = "groupId is required") String groupId,
			@NotNull(message = "messageId is required") Long messageId,
			@NotBlank(message = "pinnedBy is required") String pinnedBy) {

		PinnedMessage existing = pinnedRepo.findOne(messageId, groupId);
		if (existing != null) {
			return existing;
		}

		int count = pinnedRepo.countByGroupId(groupId);
		if (count >= MAX_PIN_COUNT) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pinned messages limit reached for this group");
		}

		PinnedMessage pm = PinnedMessage.builder().messageId(messageId).groupId(groupId).pinnedBy(pinnedBy).build();
		pinnedRepo.insert(pm);

		return pinnedRepo.findOne(messageId, groupId);
	}

	public void unpinMessage(@NotBlank(message = "groupId is required") String groupId,
			@NotNull(message = "messageId is required") Long messageId) {
		pinnedRepo.delete(messageId, groupId);
	}

	public List<PinnedMessage> getPinnedMessages(@NotBlank(message = "groupId is required") String groupId) {
		return pinnedRepo.findByGroupId(groupId);
	}

	public void pinAndBroadcast(@NotBlank(message = "groupId is required") String groupId,
			@NotNull(message = "messageId is required") Long messageId,
			@NotBlank(message = "pinnedBy is required") String pinnedBy) {
		pinMessage(groupId, messageId, pinnedBy);
		broadcastPinnedList(groupId);
	}

	public void unpinAndBroadcast(@NotBlank(message = "groupId is required") String groupId,
			@NotNull(message = "messageId is required") Long messageId) {
		unpinMessage(groupId, messageId);
		broadcastPinnedList(groupId);
	}

	private void broadcastPinnedList(String groupId) {
		List<PinnedMessage> updated = getPinnedMessages(groupId);
		messagingTemplate.convertAndSend("/topic/groups." + groupId + ".pinned", updated);
	}
}
