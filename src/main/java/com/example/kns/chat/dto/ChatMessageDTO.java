package com.example.kns.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {
	private Long senderId;
	private String groupId;
	private String content;
}
