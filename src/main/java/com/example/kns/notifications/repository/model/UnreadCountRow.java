package com.example.kns.notifications.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountRow {
	private String groupId;
	private String userId;
	private Integer unreadCount;
}
