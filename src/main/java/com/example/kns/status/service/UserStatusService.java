package com.example.kns.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStatusService {

	private static final long OFFLINE_TIMEOUT_MS = 10 * 60 * 1000; // 10 minutes

	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	private final ConcurrentHashMap<String, Long> onlineUsers = new ConcurrentHashMap<>();

	public void updateUserPing(String userId) {
		boolean isNewUser = !onlineUsers.containsKey(userId);
		onlineUsers.put(userId, Instant.now().toEpochMilli());

		if (isNewUser) {
			broadcastOnlineUsers();
		}
	}

	public Set<String> getOnlineUserIds() {
		return onlineUsers.keySet();
	}

	@Scheduled(fixedRate = 60000) // Check every minute
	public void removeOfflineUsers() {
		long now = Instant.now().toEpochMilli();

		boolean removed = onlineUsers.entrySet().removeIf(entry -> (now - entry.getValue()) > OFFLINE_TIMEOUT_MS);

		if (removed) {
			broadcastOnlineUsers();
		}
	}

	public void broadcastOnlineUsers() {
		Set<String> users = getOnlineUserIds();
		messagingTemplate.convertAndSend("/topic/online-users", users);
	}
}
