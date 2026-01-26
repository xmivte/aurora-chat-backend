package com.example.kns.encryption.service;

import com.example.kns.encryption.dto.SenderKeyAvailableDTO;
import com.example.kns.encryption.model.SenderKeyRequest;
import com.example.kns.encryption.repository.SenderKeyRequestRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@Validated
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI beans")
public class SenderKeyService {

	private final SenderKeyRequestRepository requestRepo;
	private final SimpMessagingTemplate messagingTemplate;

	public void requestAndBroadcast(String chatId, String requesterUserId, String requesterDeviceId) {
		SenderKeyRequest request = SenderKeyRequest.builder().chatId(chatId).requesterUserId(requesterUserId)
				.requesterDeviceId(requesterDeviceId).build();

		requestRepo.insertIfNotExists(request);

		messagingTemplate.convertAndSend("/topic/groups." + chatId + ".senderkey.requests",
				Map.of("chatId", chatId, "requesterUserId", requesterUserId, "requesterDeviceId", requesterDeviceId));
	}

	public void notifyDeviceSenderKeyAvailable(String chatId, String toDeviceId) {
		messagingTemplate.convertAndSend("/topic/device." + toDeviceId + ".senderkey.available",
				SenderKeyAvailableDTO.builder().chatId(chatId).toDeviceId(toDeviceId).build());
	}

	public void markRequestFulfilledIfExists(String chatId, String requesterUserId, String requesterDeviceId) {
		SenderKeyRequest pending = requestRepo.findPendingRequest(chatId, requesterUserId, requesterDeviceId);
		if (pending != null) {
			requestRepo.markFulfilled(pending.getId(), OffsetDateTime.now());
		}
	}
}