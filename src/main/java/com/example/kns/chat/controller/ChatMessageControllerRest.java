package com.example.kns.chat.controller;

import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.service.ChatMessageService;
import com.example.kns.file.dto.FileAttachmentDTO;
import com.example.kns.file.mapper.FileAttachmentMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Validated
public class ChatMessageControllerRest {

	private final ChatMessageService service;

	@GetMapping("/{groupId}")
	public List<ChatMessageDTO> getMessages(@PathVariable @NotBlank String groupId) {
		return service.getAll(groupId).stream().map(msg -> {
			List<FileAttachmentDTO> fileAttachments = FileAttachmentMapper.toDTOList(msg.getFileAttachments());

			return ChatMessageDTO.builder().id(msg.getId()).senderId(msg.getSenderId()).groupId(msg.getGroupId())
					.content(msg.getContent()).createdAt(msg.getCreatedAt()).username(msg.getUsername())
					.fileAttachments(fileAttachments).build();
		}).toList();
	}
}
