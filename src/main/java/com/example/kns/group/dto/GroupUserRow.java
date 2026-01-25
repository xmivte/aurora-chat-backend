package com.example.kns.group.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupUserRow {
	private String groupId;
	private String groupName;
	private String groupImage;
	private String userId;
	private String username;
	private String userImage;
}
