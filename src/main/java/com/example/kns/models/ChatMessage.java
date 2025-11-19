package com.example.kns.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {
	private Long id;
	private Long senderId;
	private Long receiverId; // null group chatams
	private String groupId; // null private chatams
	private String content; // KAIP SU ENCRYPTION?
	private LocalDateTime createdAt;
	private boolean sent; // tikrina ar jau issiusta zinute
}