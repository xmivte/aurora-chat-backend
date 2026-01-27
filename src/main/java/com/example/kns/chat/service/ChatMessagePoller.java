package com.example.kns.chat.service;

import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import com.example.kns.file.dto.FileAttachmentDTO;
import com.example.kns.file.mapper.FileAttachmentMapper;
import com.example.kns.file.model.FileAttachment;
import com.example.kns.file.repository.FileAttachmentRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class ChatMessagePoller {

	private static final int DEFAULT_BATCH_SIZE = 50;

	private final ChatMessagesRepository repository;
	private final FileAttachmentRepository fileAttachmentRepository;
	private final SimpMessagingTemplate messagingTemplate;

	@Scheduled(fixedRate = 1000)
	public void pollOnce() {
		pollMessagesAndBroadcast(DEFAULT_BATCH_SIZE);
	}

	/**
	 * Public entrypoint for tests/manual triggering.
	 */
	public void pollMessagesAndBroadcast() {
		pollMessagesAndBroadcast(DEFAULT_BATCH_SIZE);
	}

	/**
	 * Allows tests to control batch size if needed.
	 */
	public void pollMessagesAndBroadcast(int batchSize) {
		int size = batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;

		List<ChatMessage> fresh = repository.findUnsentMessages(size);
		if (fresh == null || fresh.isEmpty()) {
			return;
		}

		List<Long> messageIds = fresh.stream().filter(msg -> msg != null && msg.getId() != null).map(ChatMessage::getId)
				.collect(Collectors.toList());

		Map<Long, List<FileAttachment>> attachmentsByMessageId = new HashMap<>();
		if (!messageIds.isEmpty()) {
			List<FileAttachment> allAttachments = fileAttachmentRepository.findByMessageIds(messageIds);
			for (FileAttachment attachment : allAttachments) {
				attachmentsByMessageId.computeIfAbsent(attachment.getMessageId(), k -> new ArrayList<>())
						.add(attachment);
			}
		}

		for (ChatMessage msg : fresh) {
			if (msg == null || msg.getId() == null || msg.getGroupId() == null) {
				continue;
			}
			List<FileAttachment> attachments = attachmentsByMessageId.getOrDefault(msg.getId(), List.of());

			List<FileAttachmentDTO> fileMetadata = FileAttachmentMapper.toDTOList(attachments);

			ChatMessageDTO dto = ChatMessageDTO.builder().id(msg.getId()).senderId(msg.getSenderId())
					.groupId(msg.getGroupId()).content(msg.getContent()).createdAt(msg.getCreatedAt())
					.username(msg.getUsername()).fileAttachments(fileMetadata).build();

			messagingTemplate.convertAndSend("/topic/chat." + msg.getGroupId(), dto);
			repository.markAsSent(msg.getId());
		}
	}
}
