package com.example.kns.encryption.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class DeviceKeyDTO {
	private String userId;

	@JsonProperty("senderDeviceId")
	private String senderDeviceId;

	@JsonProperty("identityPublicKey")
	private Map<String, Object> identityPublicKey;

	private static <K, V> Map<K, V> copyMap(Map<K, V> in) {
		return in == null ? null : Map.copyOf(in);
	}

	// Explicit constructor to ensure defensive copy
	public DeviceKeyDTO(String userId, String senderDeviceId, Map<String, Object> identityPublicKey) {
		this.userId = userId;
		this.senderDeviceId = senderDeviceId;
		this.identityPublicKey = copyMap(identityPublicKey);
	}

	// Defensive write/read
	public void setIdentityPublicKey(Map<String, Object> identityPublicKey) {
		this.identityPublicKey = copyMap(identityPublicKey);
	}

	public Map<String, Object> getIdentityPublicKey() {
		return copyMap(this.identityPublicKey);
	}

	// Custom builder method to defensive-copy
	public static class DeviceKeyDTOBuilder {
		private Map<String, Object> identityPublicKey;

		public DeviceKeyDTOBuilder identityPublicKey(Map<String, Object> identityPublicKey) {
			this.identityPublicKey = copyMap(identityPublicKey);
			return this;
		}
	}
}
