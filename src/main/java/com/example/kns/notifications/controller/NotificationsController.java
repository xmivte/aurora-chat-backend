package com.example.kns.notifications.controller;

import com.example.kns.notifications.repository.UserGroupsRepository;
import com.example.kns.notifications.repository.model.UnreadCountRow;
import com.example.kns.notifications.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Validated
public class NotificationsController {

	private final UserGroupsRepository userGroupsRepository;
	private final NotificationService notificationService;

	@GetMapping("/unread")
	public Map<String, Integer> getUnreadCounts(@AuthenticationPrincipal Jwt jwt) {
		String userId = (jwt == null) ? null : jwt.getSubject();
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Missing authenticated user id");
		}

		List<UnreadCountRow> rows = userGroupsRepository.findUnreadCountsByUserId(userId);
		Map<String, Integer> out = new HashMap<>();

		for (UnreadCountRow r : rows) {
			if (r == null) {
				continue;
			}

			String groupId = r.getGroupId();
			if (groupId == null) {
				continue;
			}

			Integer rowUnread = r.getUnreadCount();
			int unread = (rowUnread == null) ? 0 : rowUnread;

			out.put(groupId, unread);
		}

		return out;
	}

	public record MarkReadRequest(@NotBlank(message = "groupId is blank") String groupId) {
	}

	@PostMapping("/read")
	public Map<String, Object> markRead(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid MarkReadRequest req) {
		String userId = (jwt == null) ? null : jwt.getSubject();
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Missing authenticated user id");
		}
		if (req == null || StringUtils.isBlank(req.groupId())) {
			throw new IllegalArgumentException("Group id is blank");
		}

		notificationService.markRead(userId, req.groupId());
		return Map.of("groupId", req.groupId(), "unreadCount", 0);
	}
}
