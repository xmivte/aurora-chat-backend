package com.example.kns.controllers;

import com.example.kns.models.ChatMessage;
import com.example.kns.repositories.ChatMessagesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessagesMapper mapper;

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public ChatMessage handleChat(ChatMessage message) {
        mapper.insert(message);
        return message;
    }
}
