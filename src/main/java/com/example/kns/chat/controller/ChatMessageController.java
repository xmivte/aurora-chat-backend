package com.example.kns.chat.controller;

import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class ChatMessageController {

	private final ChatMessageService service;

	@MessageMapping("/send.message")
	public void handleMessage(@Payload @Valid ChatMessageDTO dto) {
		service.saveIncoming(dto);
	}
}
