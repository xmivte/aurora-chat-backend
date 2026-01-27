package com.example.kns.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ServerGroupUserRow {
	private String groupId;
	private String groupName;
	private String groupImage;
	private Long serverId;
	private String userId;
	private String userEmail;
	private String username;
	private String userImage;
}
