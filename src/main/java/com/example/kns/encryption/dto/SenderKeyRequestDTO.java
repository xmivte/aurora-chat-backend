package com.example.kns.encryption.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SenderKeyRequestDTO {
	@NotBlank(message = "requesterUserId is required")
	private String requesterUserId;

	@NotBlank(message = "requesterDeviceId is required")
	private String requesterDeviceId;
}
