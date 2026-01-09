package com.example.kns.chat.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class TypingService {

	private final SimpMessagingTemplate messagingTemplate;
	ConcurrentHashMap<String, Set<String>> typingUsers = new ConcurrentHashMap<>();
	ConcurrentHashMap<String, Set<String>> typingUserGroups = new ConcurrentHashMap<>();

	public void updateUserTyping(String groupId, String userId, Boolean typing) {
		boolean exists = typingUsers.getOrDefault(groupId, Set.of()).contains(userId);

		if (!exists && typing) {
			typingUsers.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).add(userId);
			typingUserGroups.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(groupId);
		} else if (exists && !typing) {
			typingUsers.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).remove(userId);
			typingUserGroups.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).remove(groupId);
		}
		broadcastTypingUsers(groupId);
	}

	public void disconnectUser(String userId) {
		var groups = typingUserGroups.getOrDefault(userId, Set.of());
		for (String groupId : groups) {
			updateUserTyping(groupId, userId, false);
			broadcastTypingUsers(groupId);
		}
		typingUserGroups.remove(userId);
	}

	public Set<String> getTypingUserIds(String groupId) {
		return typingUsers.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet());
	}

	public void broadcastTypingUsers(String groupId) {
		Set<String> users = getTypingUserIds(groupId);
		messagingTemplate.convertAndSend("/topic/typing-users/" + groupId, users);
	}
}