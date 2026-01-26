package com.example.kns.encryption.service;

import com.example.kns.encryption.dto.RegisterDeviceKeyDTO;
import com.example.kns.encryption.model.DeviceKey;
import com.example.kns.encryption.repository.DeviceKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceKeyServiceTest {

	@Test
	void registerDeviceKey_WithValidDto_EnsuresDeviceAndUpserts() throws Exception {
		DeviceKeyRepository repo = mock(DeviceKeyRepository.class);
		ObjectMapper mapper = new ObjectMapper();

		DeviceKeyService service = new DeviceKeyService(repo, mapper);

		RegisterDeviceKeyDTO dto = new RegisterDeviceKeyDTO();
		dto.setUserId("u1");
		dto.setSenderDeviceId("d1");
		dto.setIdentityPublicKey(Map.of("kty", "EC", "crv", "P-256"));

		service.registerDeviceKey(dto);

		verify(repo).ensureDeviceExists("d1");

		ArgumentCaptor<DeviceKey> captor = ArgumentCaptor.forClass(DeviceKey.class);
		verify(repo).upsert(captor.capture());

		DeviceKey saved = captor.getValue();
		assertThat(saved.getUserId()).isEqualTo("u1");
		assertThat(saved.getDeviceId()).isEqualTo("d1");

		String expectedJson = mapper.writeValueAsString(dto.getIdentityPublicKey());
		assertThat(saved.getIdentityKeyPublic()).isEqualTo(expectedJson);
	}

	@Test
	void getUserDevices_WithStoredJson_ReturnsMappedDtos() throws Exception {
		DeviceKeyRepository repo = mock(DeviceKeyRepository.class);
		ObjectMapper mapper = new ObjectMapper();
		DeviceKeyService service = new DeviceKeyService(repo, mapper);

		String json = mapper.writeValueAsString(Map.of("x", "y"));

		DeviceKey model = DeviceKey.builder().deviceId("dev-123").userId("user-123").identityKeyPublic(json).build();

		when(repo.findByUserId("user-123")).thenReturn(List.of(model));

		var result = service.getUserDevices("user-123");

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getUserId()).isEqualTo("user-123");
		assertThat(result.get(0).getSenderDeviceId()).isEqualTo("dev-123");
		assertThat(result.get(0).getIdentityPublicKey()).containsEntry("x", "y");
	}
}
