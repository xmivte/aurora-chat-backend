package com.example.kns.controllers;

import com.example.kns.dto.ChatMessageDTO;
import com.example.kns.models.ChatMessage;
import com.example.kns.repositories.ChatMessagesMapper;
import com.example.kns.services.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService service;

	@MessageMapping("/send.message")
	public void handleMessage(ChatMessageDTO dto) {
		// Reikia kad is JWT istrauktu userId ir kad sukurtu principal
		ChatMessage saved = service.saveIncoming(dto);
		// Real time delivery daro @Scheduled polleris
	}
}
