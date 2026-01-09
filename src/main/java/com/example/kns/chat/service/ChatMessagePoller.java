package com.example.kns.chat.service;

import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class ChatMessagePoller {

	private static final int DEFAULT_BATCH_SIZE = 50;

	private final ChatMessagesRepository repository;
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

		for (ChatMessage msg : fresh) {
			if (msg == null || msg.getId() == null || msg.getGroupId() == null) {
				continue;
			}
			messagingTemplate.convertAndSend("/topic/chat." + msg.getGroupId(), msg);
			repository.markAsSent(msg.getId());
		}
	}
}
