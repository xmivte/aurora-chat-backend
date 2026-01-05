package com.example.kns.pin_message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PinnedMessageDTO {
	@NotNull(message = "messageId is required")
	private Long messageId;

	@NotBlank(message = "pinnedBy is required")
	private String pinnedBy;
}