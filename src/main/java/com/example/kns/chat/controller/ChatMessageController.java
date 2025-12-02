package com.example.kns.chat.controller;

import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService service;

	@MessageMapping("/send.message")
	public void handleMessage(ChatMessageDTO dto) {
		// Future: Extract userId from JWT and attach it to the WebSocket Principal
		service.saveIncoming(dto);
		// Note: Actual real-time delivery is done by the @Scheduled poller
	}
}
