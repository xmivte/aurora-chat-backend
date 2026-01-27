package com.example.kns.group.controller;

import com.example.kns.dto.UserContext;
import com.example.kns.group.dto.CreateGroupRequest;
import com.example.kns.group.dto.GroupDTO;
import com.example.kns.group.dto.GroupWithUsersDTO;
import com.example.kns.group.dto.ServerGroupWithUsersDTO;
import com.example.kns.group.model.Group;
import com.example.kns.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

	private final GroupService service;

	@GetMapping("/{userId}")
	public List<GroupWithUsersDTO> getGroups(@PathVariable String userId) {
		return service.getAllWithUsers(userId);
	}

	@GetMapping("/server/{userId}")
	public List<ServerGroupWithUsersDTO> getServerGroups(@AuthenticationPrincipal UserContext userContext) {
		return service.getAllServerGroupsWithUsers(userContext.getEmail());
	}

	@PostMapping
	public GroupDTO createGroup(@RequestBody CreateGroupRequest request) {
		return service.createGroup(request.getMyUserId(), request.getOtherUserId());
	}

	@GetMapping("/{groupId}/participants")
	public List<String> getParticipants(@PathVariable String groupId) {
		return service.getParticipantIds(groupId);
	}

}
