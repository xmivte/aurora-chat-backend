package com.example.kns.chat.controller;

import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class ChatMessageControllerRest {

	private final ChatMessageService service;

	@GetMapping("/{groupId}")
	public List<ChatMessageDTO> getMessages(@PathVariable String groupId) {
		return service.getAll(groupId).stream()
				.map(msg -> ChatMessageDTO.builder().id(msg.getId()).senderId(msg.getSenderId())
						.groupId(msg.getGroupId()).content(msg.getContent()).createdAt(msg.getCreatedAt()).username(msg.getUsername()).build())
				.toList();
	}
}
