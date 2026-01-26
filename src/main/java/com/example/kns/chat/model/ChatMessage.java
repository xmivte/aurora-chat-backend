package com.example.kns.chat.model;

import com.example.kns.file.model.FileAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
	private Long id;
	private String senderId;
	private String groupId; // Used for both public + private chats
	private String content; // Future: needs encryption
	private OffsetDateTime createdAt;
	private boolean sent; // Poller checks if the message has been sent
	private String username;
	private List<FileAttachment> fileAttachments;
}