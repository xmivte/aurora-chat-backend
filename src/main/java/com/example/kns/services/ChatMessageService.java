package com.example.kns.services;

import com.example.kns.dto.ChatMessageDTO;
import com.example.kns.models.ChatMessage;
import com.example.kns.repositories.ChatMessagesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private static final int MAX_MESSAGE_LENGTH = 2000;
	private final ChatMessagesMapper mapper;
	private final SimpMessagingTemplate messagingTemplate;

	@Scheduled(fixedRate = 1000)
	public void pollMessagesAndBroadcast() {
		List<ChatMessage> fresh = mapper.findUnsentMessages(50);

		for (ChatMessage msg : fresh) {
			if (msg.getGroupId() != null) {
				// Public chats
				messagingTemplate.convertAndSend("/topic/chat." + msg.getGroupId(), msg);
			} else if (msg.getReceiverId() != null) {
				// Private chats
				// messagingTemplate.convertAndSendToUser(msg.getReceiverId().toString(),
				// "/queue/messages", msg);
			}
			mapper.markAsSent(msg.getId());
		}
	}

	public ChatMessage saveIncoming(ChatMessageDTO dto) {

		if (dto.getContent() == null || dto.getContent().isBlank()) {
			throw new IllegalArgumentException("Message content is empty");
		}

		if (dto.getContent().length() > MAX_MESSAGE_LENGTH) {
			throw new IllegalArgumentException("Message is too long (max 2000 chars)");
		}

		ChatMessage msg = new ChatMessage();
		msg.setSenderId(dto.getSenderId());
		msg.setReceiverId(dto.getReceiverId());
		msg.setGroupId(dto.getGroupId());
		msg.setContent(dto.getContent());
		msg.setSent(false);

		mapper.insert(msg);
		return msg;
	}
}
