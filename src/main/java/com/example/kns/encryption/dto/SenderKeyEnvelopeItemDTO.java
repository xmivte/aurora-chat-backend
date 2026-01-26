package com.example.kns.encryption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SenderKeyEnvelopeItemDTO {
	@NotBlank
	private String chatId;

	@NotBlank
	private String fromUserId;

	@NotBlank
	private String fromDeviceId;

	@NotBlank
	private String toUserId;

	@NotBlank
	private String toDeviceId;

	@NotNull
	private Map<String, Object> wrapped;

	private static <K, V> Map<K, V> copyMap(Map<K, V> in) {
		return in == null ? null : Map.copyOf(in);
	}

	public void setWrapped(Map<String, Object> wrapped) {
		this.wrapped = copyMap(wrapped);
	}

	public Map<String, Object> getWrapped() {
		return copyMap(this.wrapped);
	}
}
