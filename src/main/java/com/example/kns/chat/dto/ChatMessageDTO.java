package com.example.kns.chat.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class ChatMessageDTO {
	private Long id;
	private Long senderId;
	private String groupId;
	private String content;
	private OffsetDateTime createdAt;
	private String username;
}