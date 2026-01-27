package com.example.kns.controllers;

import com.example.kns.dto.UserContext;
import com.example.kns.dto.UserDataDto;
import com.example.kns.services.UserAccountService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class UserAccountController {
	private final UserAccountService userService;

	@GetMapping()
	public ResponseEntity<UserDataDto> fetchUser(@AuthenticationPrincipal UserContext userContext) {
		final var usersData = userService.getUser(userContext);
		return ResponseEntity.ok(usersData);
	}

	@PostMapping()
	public ResponseEntity<UserDataDto> createUser(@AuthenticationPrincipal UserContext userContext,
			@RequestBody UserDataDto usersDataDto) {
		final var createdUser = userService.createUser(userContext, usersDataDto);
		return ResponseEntity.ok(createdUser);
	}

	@DeleteMapping
	public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserContext userContext) {
		userService.deleteUser(userContext);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{newUsername}")
	public ResponseEntity<UserDataDto> updateUsername(@AuthenticationPrincipal UserContext userContext,
			@PathVariable String newUsername) {
		final var updatedUser = userService.updateUsersUsername(userContext, newUsername);
		return ResponseEntity.ok(updatedUser);
	}
}