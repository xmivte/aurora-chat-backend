package com.example.kns.chat.service;

import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private static final int MAX_MESSAGE_LENGTH = 2000;
	private final ChatMessagesRepository mapper;
	private final SimpMessagingTemplate messagingTemplate;

	@Scheduled(fixedRate = 1000)
	public void pollMessagesAndBroadcast() {
		List<ChatMessage> fresh = mapper.findUnsentMessages(50);

		for (ChatMessage msg : fresh) {
			messagingTemplate.convertAndSend("/topic/chat." + msg.getGroupId(), msg);
			mapper.markAsSent(msg.getId());
		}
	}

	public ChatMessage saveIncoming(ChatMessageDTO dto) {

		var content = dto.getContent();

		if (StringUtils.isBlank(content)) {
			throw new IllegalArgumentException("Message content is empty");
		}

		if (content.length() > MAX_MESSAGE_LENGTH) {
			throw new IllegalArgumentException("Message is too long (max 2000 chars)");
		}

		ChatMessage msg = ChatMessage.builder().senderId(dto.getSenderId()).groupId(dto.getGroupId())
				.content(dto.getContent()).sent(false).build();

		mapper.insert(msg);
		return msg;
	}

	public List<ChatMessage> getAll(String groupId) {
		if (StringUtils.isBlank(groupId)) {
			throw new IllegalArgumentException("Group id is blank");
		}
		return mapper.findAllMessagesByGroupId(groupId);
	}
}
