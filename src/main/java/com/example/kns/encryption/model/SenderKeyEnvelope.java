package com.example.kns.encryption.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SenderKeyEnvelope {
	private Long id;
	private String chatId;
	private String fromUserId;
	private String fromDeviceId;
	private String toUserId;
	private String toDeviceId;
	private String wrapped;
	private OffsetDateTime createdAt;
	private OffsetDateTime consumedAt;
}
