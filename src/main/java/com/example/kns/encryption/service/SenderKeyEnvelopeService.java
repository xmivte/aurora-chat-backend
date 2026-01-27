package com.example.kns.encryption.service;

import com.example.kns.encryption.dto.DistributeSenderKeysDTO;
import com.example.kns.encryption.dto.SenderKeyEnvelopeDTO;
import com.example.kns.encryption.dto.SenderKeyEnvelopeItemDTO;
import com.example.kns.encryption.model.SenderKeyEnvelope;
import com.example.kns.encryption.repository.SenderKeyEnvelopeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI beans")
public class SenderKeyEnvelopeService {

	private static final TypeReference<Map<String, Object>> MAP_REF = new TypeReference<>() {
	};

	private final SenderKeyEnvelopeRepository envelopeRepo;
	private final SenderKeyService senderKeyService;
	private final ObjectMapper objectMapper;

	public Optional<SenderKeyEnvelopeDTO> getPending(String chatId, String userId, String deviceId) {
		SenderKeyEnvelope envelope = envelopeRepo.findPending(chatId, userId, deviceId);
		if (envelope == null)
			return Optional.empty();

		return Optional.of(SenderKeyEnvelopeDTO.builder().id(envelope.getId()).chatId(envelope.getChatId())
				.fromUserId(envelope.getFromUserId()).fromDeviceId(envelope.getFromDeviceId())
				.toUserId(envelope.getToUserId()).toDeviceId(envelope.getToDeviceId())
				.wrapped(readJson(envelope.getWrapped())).build());
	}

	public void consume(Long id) {
		envelopeRepo.consume(id, OffsetDateTime.now());
	}

	public void distribute(DistributeSenderKeysDTO dto) {
		for (SenderKeyEnvelopeItemDTO item : dto.getItems()) {
			String wrappedJson = writeJson(item.getWrapped());

			SenderKeyEnvelope envelope = SenderKeyEnvelope.builder().chatId(item.getChatId())
					.fromUserId(item.getFromUserId()).fromDeviceId(item.getFromDeviceId()).toUserId(item.getToUserId())
					.toDeviceId(item.getToDeviceId()).wrapped(wrappedJson).build();

			envelopeRepo.insert(envelope);

			senderKeyService.markRequestFulfilledIfExists(item.getChatId(), item.getToUserId(), item.getToDeviceId());

			senderKeyService.notifyDeviceSenderKeyAvailable(item.getChatId(), item.getToDeviceId());
		}
	}

	private String writeJson(Map<String, Object> value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize wrapped key", e);
		}
	}

	private Map<String, Object> readJson(String json) {
		try {
			return objectMapper.readValue(json, MAP_REF);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to parse wrapped key", e);
		}
	}
}
