package com.example.kns.chat.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class ChatMessage {
	private Long id;
	private String senderId;
	private String groupId; // Used for both public + private chats
	private String content; // Future: needs encryption
	private OffsetDateTime createdAt;
	private boolean sent; // Poller checks if the message has been sent
	private String username;
}