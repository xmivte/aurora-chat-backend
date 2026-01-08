package com.example.kns.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import org.springframework.messaging.simp.annotation.SubscribeMapping;
import java.util.Set;

@Controller
public class UserStatusController {

	@Autowired
	private UserStatusService userStatusService;

	@MessageMapping("/user.ping")
	public void ping(Principal principal) {
		if (principal != null) {
			userStatusService.updateUserPing(principal.getName());
		}
	}

	@SubscribeMapping("/online-users")
	public Set<String> getOnlineUsers() {
		return userStatusService.getOnlineUserIds();
	}
}
