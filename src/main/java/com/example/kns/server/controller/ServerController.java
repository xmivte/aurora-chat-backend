package com.example.kns.server.controller;

import com.example.kns.dto.UserContext;
import com.example.kns.group.dto.GroupDTO;
import com.example.kns.server.dto.ServerDTO;
import com.example.kns.server.service.ServerService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/server")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class ServerController {

	private final ServerService service;

	@GetMapping("/{userId}")
	public List<ServerDTO> getServers(@AuthenticationPrincipal UserContext userContext) {
		return service.getAll(userContext.getEmail()).stream()
				.map(server -> ServerDTO.builder().id(server.getId()).name(server.getName())
						.userEmail(server.getUserEmail()).backgroundColorHex(server.getBackgroundColorHex()).build())
				.toList();
	}

	@DeleteMapping("/{serverId}")
	public void deleteServer(@PathVariable Long serverId, @AuthenticationPrincipal UserContext userContext) {
		service.deleteServer(serverId, userContext.getEmail());
	}

	@PostMapping
	public ServerDTO postServer(@RequestBody ServerDTO serverDTO, @AuthenticationPrincipal UserContext userContext) {
		return service.insertServer(userContext.getEmail(), serverDTO.getName());
	}
}
