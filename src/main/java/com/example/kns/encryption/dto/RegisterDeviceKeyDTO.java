package com.example.kns.encryption.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RegisterDeviceKeyDTO {
	@NotBlank
	private String userId;

	@NotBlank
	@JsonProperty("senderDeviceId")
	private String senderDeviceId;

	@NotNull
	@JsonProperty("identityPublicKey")
	private Map<String, Object> identityPublicKey;

	private static <K, V> Map<K, V> copyMap(Map<K, V> in) {
		return in == null ? null : Map.copyOf(in);
	}

	public void setIdentityPublicKey(Map<String, Object> identityPublicKey) {
		this.identityPublicKey = copyMap(identityPublicKey);
	}

	public Map<String, Object> getIdentityPublicKey() {
		return copyMap(this.identityPublicKey);
	}
}
