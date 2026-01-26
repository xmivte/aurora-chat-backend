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
public class SenderKeyRequest {
	private Long id;
	private String chatId;
	private String requesterUserId;
	private String requesterDeviceId;
	private OffsetDateTime createdAt;
	private OffsetDateTime fulfilledAt;
}
