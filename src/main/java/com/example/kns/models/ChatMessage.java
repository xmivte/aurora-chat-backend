package com.example.kns.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ChatMessage {
    private Long id;
    private String sender;
    private String content;
    private Date createdAt;
}