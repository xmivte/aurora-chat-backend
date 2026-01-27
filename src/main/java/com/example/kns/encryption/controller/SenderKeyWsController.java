package com.example.kns.encryption.controller;

import com.example.kns.encryption.dto.SenderKeyRequestDTO;
import com.example.kns.encryption.service.SenderKeyService;
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
public class SenderKeyWsController {

	private final SenderKeyService service;

	@MessageMapping("/groups.{groupId}.senderkey.request")
	public void request(@DestinationVariable @NotBlank String groupId, @Valid SenderKeyRequestDTO dto) {
		service.requestAndBroadcast(groupId, dto.getRequesterUserId(), dto.getRequesterDeviceId());
	}
}
