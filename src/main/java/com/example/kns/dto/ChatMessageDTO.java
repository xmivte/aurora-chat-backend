package com.example.kns.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {
	private Long senderId;
	private Long receiverId;
	private String groupId;
	private String content;
}
