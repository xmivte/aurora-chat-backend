package com.example.kns.pin_message.controller;

import com.example.kns.pin_message.dto.PinnedMessageDTO;
import com.example.kns.pin_message.model.PinnedMessage;
import com.example.kns.pin_message.service.PinnedMessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/pinned-messages")
public class PinnedMessageController {

	private final PinnedMessageService service;

	@PostMapping
	public PinnedMessage pin(@PathVariable @NotBlank(message = "groupId is required") String groupId,
			@RequestBody @Valid PinnedMessageDTO request) {
		return service.pinMessage(groupId, request.getMessageId(), request.getPinnedBy());
	}

	@GetMapping
	public List<PinnedMessage> getPinned(@PathVariable @NotBlank(message = "groupId is required") String groupId) {
		return service.getPinnedMessages(groupId);
	}

	@DeleteMapping("/{messageId}")
	public void unpin(@PathVariable @NotBlank(message = "groupId is required") String groupId,
			@PathVariable @NotNull(message = "messageId is required") Long messageId) {
		service.unpinMessage(groupId, messageId);
	}
}