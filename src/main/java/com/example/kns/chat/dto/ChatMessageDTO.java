package com.example.kns.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

	private Long id;

	@NotBlank(message = "senderId is blank")
	private String senderId;

	@NotBlank(message = "groupId is blank")
	private String groupId;

	@NotBlank(message = "content is empty")
	@Size(max = 2000, message = "Message is too long (max 2000 chars)")
	private String content;

	private OffsetDateTime createdAt;
	private String username;
}
