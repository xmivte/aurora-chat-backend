package com.example.kns.encryption.service;

import com.example.kns.encryption.dto.DeviceKeyDTO;
import com.example.kns.encryption.dto.RegisterDeviceKeyDTO;
import com.example.kns.encryption.model.DeviceKey;
import com.example.kns.encryption.repository.DeviceKeyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI beans")
public class DeviceKeyService {

	private static final TypeReference<Map<String, Object>> MAP_REF = new TypeReference<>() {
	};

	private final DeviceKeyRepository deviceKeyRepo;
	private final ObjectMapper objectMapper;

	public void registerDeviceKey(RegisterDeviceKeyDTO dto) {
		String publicKeyJson = writeJson(dto.getIdentityPublicKey());

		deviceKeyRepo.ensureDeviceExists(dto.getSenderDeviceId());

		DeviceKey deviceKey = DeviceKey.builder().deviceId(dto.getSenderDeviceId()).userId(dto.getUserId())
				.identityKeyPublic(publicKeyJson).build();

		deviceKeyRepo.upsert(deviceKey);
	}

	public List<DeviceKeyDTO> getUserDevices(String userId) {
		return deviceKeyRepo.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
	}

	private DeviceKeyDTO toDto(DeviceKey device) {
		return DeviceKeyDTO.builder().userId(device.getUserId()).senderDeviceId(device.getDeviceId())
				.identityPublicKey(readJson(device.getIdentityKeyPublic())).build();
	}

	private String writeJson(Map<String, Object> value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize public key", e);
		}
	}

	private Map<String, Object> readJson(String json) {
		try {
			return objectMapper.readValue(json, MAP_REF);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to parse public key", e);
		}
	}
}