package com.example.kns.encryption.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderKeyRequestRowDTO {
	private Long id;
	private String chatId;
	private String requesterUserId;
	private String requesterDeviceId;
	private OffsetDateTime createdAt;
}
