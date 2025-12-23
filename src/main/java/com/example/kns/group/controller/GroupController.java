package com.example.kns.group.controller;

import com.example.kns.group.dto.GroupDTO;
import com.example.kns.group.model.Group;
import com.example.kns.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

	private final GroupService service;

	@GetMapping("/{userId}")
	public List<GroupDTO> getGroups(@PathVariable String userId) {
		return service.getAll(userId).stream().map(
				group -> GroupDTO.builder().id(group.getId()).name(group.getName()).image(group.getImage()).build())
				.toList();
	}
}
