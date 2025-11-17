package com.example.kns.controllers;

import com.example.kns.models.ChatMessage;
import com.example.kns.repositories.ChatMessagesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatMessagesMapper mapper;

    @GetMapping("/api/chat/history")
    public List<ChatMessage> getHistory() {
        return mapper.findLastMessages(50);
    }
}