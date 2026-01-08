package com.example.kns.chat.controller;

import com.example.kns.chat.service.TypingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class TypingController {

	private final TypingService typingService;

	@MessageMapping("/user.typing/start/{groupId}")
	public void pingStart(@DestinationVariable String groupId, Principal principal) {
		if (principal != null) {
			typingService.updateUserTyping(groupId, principal.getName(), true);
		}
	}

	@MessageMapping("/user.typing/stop/{groupId}")
	public void pingStop(@DestinationVariable String groupId, Principal principal) {
		if (principal != null) {
			typingService.updateUserTyping(groupId, principal.getName(), false);
		}
	}

	@EventListener
	public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
		Principal user = event.getUser();
		if (user == null) {
			return;
		}
		String userId = user.getName();
		typingService.disconnectUser(userId);
	}

	@SubscribeMapping("/typing-users/{groupId}")
	public Set<String> getTypingUsers(@DestinationVariable String groupId) {
		return typingService.getTypingUserIds(groupId);
	}
}
