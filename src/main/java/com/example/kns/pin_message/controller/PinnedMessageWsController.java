package com.example.kns.pin_message.controller;

import com.example.kns.pin_message.dto.PinnedMessageDTO;
import com.example.kns.pin_message.dto.UnpinMessageDTO;
import com.example.kns.pin_message.service.PinnedMessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
@RequiredArgsConstructor
public class PinnedMessageWsController {

	private final PinnedMessageService service;

	@MessageMapping("/groups.{groupId}.pin")
	public void pin(@DestinationVariable @NotBlank(message = "groupId is required") String groupId,
			@Valid PinnedMessageDTO dto) {
		service.pinAndBroadcast(groupId, dto.getMessageId(), dto.getPinnedBy());
	}

	@MessageMapping("/groups.{groupId}.unpin")
	public void unpin(@DestinationVariable @NotBlank(message = "groupId is required") String groupId,
			@Valid UnpinMessageDTO dto) {
		service.unpinAndBroadcast(groupId, dto.getMessageId());
	}
}
