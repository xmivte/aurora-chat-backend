package com.example.kns.server.service;

import com.example.kns.group.repository.GroupRepository;
import com.example.kns.server.dto.ServerDTO;
import com.example.kns.server.model.Server;
import com.example.kns.server.repository.ServerRepository;
import com.example.kns.server_group_users.repository.ServerGroupUserRepository;
import com.example.kns.server_groups.model.ServerGroup;
import com.example.kns.server_groups.repository.ServerGroupsRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class ServerService {

	private final ServerRepository serverRepository;
	private final GroupRepository groupRepository;
	private final ServerGroupsRepository serverGroupsRepository;
	private final ServerGroupUserRepository serverGroupUserRepository;

	public List<Server> getAll(@NotBlank String userId) {
		return serverRepository.findAllServersByUserId(userId);
	}

	@Transactional
	public ServerDTO insertServer(@NotBlank String userId, String name) {
		int color = ThreadLocalRandom.current().nextInt(0x1000000);
		String colorHex = String.format("#%06X", color);

		Server server = Server.builder().name(name).userId(userId).backgroundColorHex(colorHex).build();

		serverRepository.insert(server);

		Long serverId = server.getId();
		String groupId = UUID.randomUUID().toString();
		ServerGroup serverGroup = ServerGroup.builder().serverId(serverId).groupId(groupId).build();

		groupRepository.insert(groupId, "main", null);
		serverGroupsRepository.insert(serverGroup);
		serverGroupUserRepository.insert(serverGroup.getId(), userId);

		return ServerDTO.builder().id(serverId).name(name).userId(userId).backgroundColorHex(colorHex).build();
	}

	public void deleteServer(@NotBlank Long serverId) {
		groupRepository.deleteServerGroups(serverId);
		serverRepository.deleteServer(serverId);
	}
}