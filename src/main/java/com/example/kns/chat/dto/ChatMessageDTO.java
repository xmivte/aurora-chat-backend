package com.example.kns.chat.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
	private Long id;
	private String senderId;
	private String groupId;
	private String content;
	private OffsetDateTime createdAt;
	private String username;
}