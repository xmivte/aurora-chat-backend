package com.example.kns.controllers;

import com.example.kns.dto.UserContext;
import com.example.kns.dto.UserDataDto;
import com.example.kns.services.UserAccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserAccountController {
	private final UserAccountService userService;

	@GetMapping()
	public ResponseEntity<UserDataDto> fetchUser(@AuthenticationPrincipal UserContext userContext) {
		var usersData = userService.getUser(userContext);
		return ResponseEntity.ok(usersData);
	}

	@PostMapping()
	public ResponseEntity<UserDataDto> createUser(@AuthenticationPrincipal UserContext userContext,
			@RequestBody UserDataDto usersDataDto) {
		var createdUser = userService.createUser(userContext, usersDataDto);
		return ResponseEntity.ok(createdUser);
	}
}