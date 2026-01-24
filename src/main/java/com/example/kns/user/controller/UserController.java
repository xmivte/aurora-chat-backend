package com.example.kns.user.controller;

import com.example.kns.user.dto.UserDTO;
import com.example.kns.user.model.User;
import com.example.kns.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private final UserService service;

	@GetMapping("/{groupId}")
	public List<UserDTO> getUsers(@PathVariable String groupId) {
		return service.getAll(groupId).stream().map(
				user -> UserDTO.builder().id(user.getId()).username(user.getUsername()).image(user.getImage()).build())
				.toList();
	}

	@GetMapping("/server/{serverId}")
	public List<UserDTO> getServerUsers(@PathVariable Long serverId) {
		return service.getAllServer(serverId).stream().map(
				user -> UserDTO.builder().id(user.getId()).username(user.getUsername()).image(user.getImage()).build())
				.toList();
	}

	@GetMapping("/all")
	public List<UserDTO> getAllUsers() {
		return service.getAllUsers().stream().map(
				user -> UserDTO.builder().id(user.getId()).username(user.getUsername()).image(user.getImage()).build())
				.toList();
	}
}
