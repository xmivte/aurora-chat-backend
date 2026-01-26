package com.example.kns.encryption.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class SenderKeyEnvelopeDTO {
	private Long id;
	private String chatId;
	private String fromUserId;
	private String fromDeviceId;
	private String toUserId;
	private String toDeviceId;
	private Map<String, Object> wrapped;

	private static <K, V> Map<K, V> copyMap(Map<K, V> in) {
		return in == null ? null : Map.copyOf(in);
	}

	// Explicit constructor to ensure defensive copy
	public SenderKeyEnvelopeDTO(Long id, String chatId, String fromUserId, String fromDeviceId, String toUserId,
			String toDeviceId, Map<String, Object> wrapped) {
		this.id = id;
		this.chatId = chatId;
		this.fromUserId = fromUserId;
		this.fromDeviceId = fromDeviceId;
		this.toUserId = toUserId;
		this.toDeviceId = toDeviceId;
		this.wrapped = copyMap(wrapped);
	}

	public void setWrapped(Map<String, Object> wrapped) {
		this.wrapped = copyMap(wrapped);
	}

	public Map<String, Object> getWrapped() {
		return copyMap(this.wrapped);
	}

	// Custom builder method to defensive-copy
	public static class SenderKeyEnvelopeDTOBuilder {
		private Map<String, Object> wrapped;

		public SenderKeyEnvelopeDTOBuilder wrapped(Map<String, Object> wrapped) {
			this.wrapped = copyMap(wrapped);
			return this;
		}
	}
}
