package com.example.kns.server.controller;

import com.example.kns.group.dto.GroupDTO;
import com.example.kns.server.dto.ServerDTO;
import com.example.kns.server.service.ServerService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/server")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class ServerController {

	private final ServerService service;

	@GetMapping("/{userId}")
	public List<ServerDTO> getServers(@PathVariable String userId) {
		return service
				.getAll(userId).stream().map(server -> ServerDTO.builder().id(server.getId()).name(server.getName())
						.userId(server.getUserId()).backgroundColorHex(server.getBackgroundColorHex()).build())
				.toList();
	}

	@DeleteMapping("/{serverId}")
	public void deleteServer(@PathVariable Long serverId) {
		service.deleteServer(serverId);
	}

	@PostMapping
	public ServerDTO postServer(@RequestBody ServerDTO serverDTO) {
		return service.insertServer(serverDTO.getUserId(), serverDTO.getName());
	}
}
