package com.example.kns.server.controller;

import com.example.kns.group.dto.GroupDTO;
import com.example.kns.server.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/server")
public class ServerController {

	private final ServerService service;

	@GetMapping("/{userId}")
	public List<GroupDTO> getServers(@PathVariable String userId) {
		return service.getAll(userId).stream().map(
				group -> GroupDTO.builder().id(group.getId()).name(group.getName()).image(group.getImage()).build())
				.toList();
	}
}
