package com.example.kns.pin_message.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnpinMessageDTO {
	@NotNull(message = "messageId is required")
	private Long messageId;
}
