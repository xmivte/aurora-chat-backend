package com.example.kns.encryption.service;

import com.example.kns.encryption.dto.SenderKeyAvailableDTO;
import com.example.kns.encryption.model.SenderKeyRequest;
import com.example.kns.encryption.repository.SenderKeyRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SenderKeyServiceTest {

	@Test
	void requestAndBroadcast_WithValidRequest_InsertsAndBroadcasts() {
		SenderKeyRequestRepository repo = mock(SenderKeyRequestRepository.class);
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);

		SenderKeyService service = new SenderKeyService(repo, template);

		service.requestAndBroadcast("chat1", "userA", "devA");

		ArgumentCaptor<SenderKeyRequest> reqCaptor = ArgumentCaptor.forClass(SenderKeyRequest.class);
		verify(repo).insertIfNotExists(reqCaptor.capture());

		SenderKeyRequest saved = reqCaptor.getValue();
		assertThat(saved.getChatId()).isEqualTo("chat1");
		assertThat(saved.getRequesterUserId()).isEqualTo("userA");
		assertThat(saved.getRequesterDeviceId()).isEqualTo("devA");

		ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
		verify(template).convertAndSend(eq("/topic/groups.chat1.senderkey.requests"), payloadCaptor.capture());

		Map<String, Object> payload = payloadCaptor.getValue();
		assertThat(payload).containsEntry("chatId", "chat1");
		assertThat(payload).containsEntry("requesterUserId", "userA");
		assertThat(payload).containsEntry("requesterDeviceId", "devA");
	}

	@Test
	void markRequestFulfilledIfExists_WithPendingRequest_MarksFulfilled() {
		SenderKeyRequestRepository repo = mock(SenderKeyRequestRepository.class);
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		SenderKeyService service = new SenderKeyService(repo, template);

		SenderKeyRequest pending = SenderKeyRequest.builder().id(99L).chatId("c1").requesterUserId("u1")
				.requesterDeviceId("d1").build();

		when(repo.findPendingRequest("c1", "u1", "d1")).thenReturn(pending);

		service.markRequestFulfilledIfExists("c1", "u1", "d1");

		verify(repo).markFulfilled(eq(99L), any(OffsetDateTime.class));
	}

	@Test
	void notifyDeviceSenderKeyAvailable_WithValidDevice_SendsToTopic() {
		SenderKeyRequestRepository repo = mock(SenderKeyRequestRepository.class);
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		SenderKeyService service = new SenderKeyService(repo, template);

		service.notifyDeviceSenderKeyAvailable("chatX", "devX");

		ArgumentCaptor<SenderKeyAvailableDTO> dtoCaptor = ArgumentCaptor.forClass(SenderKeyAvailableDTO.class);
		verify(template).convertAndSend(eq("/topic/device.devX.senderkey.available"), dtoCaptor.capture());

		SenderKeyAvailableDTO dto = dtoCaptor.getValue();
		assertThat(dto.getChatId()).isEqualTo("chatX");
		assertThat(dto.getToDeviceId()).isEqualTo("devX");
	}
}
