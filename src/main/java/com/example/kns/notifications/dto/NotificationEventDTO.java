package com.example.kns.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventDTO {
	private String type;
	private String groupId;
	private String fromUserId;
	private Long messageId;
	private String content;
	private Instant timestamp;
	private Integer unreadCount;
}
