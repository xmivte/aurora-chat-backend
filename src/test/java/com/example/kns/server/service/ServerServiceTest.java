package com.example.kns.server.service;

import com.example.kns.group.dto.ServerGroupUserRow;
import com.example.kns.group.model.Group;
import com.example.kns.group.service.GroupService;
import com.example.kns.server.dto.ServerDTO;
import com.example.kns.server.model.Server;
import com.example.kns.server.repository.ServerRepository;
import com.example.kns.server_group_users.repository.ServerGroupUserRepository;
import com.example.kns.server_groups.model.ServerGroup;
import com.example.kns.server_groups.repository.ServerGroupsRepository;
import com.example.kns.user.model.User;
import com.example.kns.group.repository.GroupRepository;
import com.example.kns.user.repository.UserRepository;
import com.example.kns.user_groups.repository.UserGroupRepository;
import com.example.kns.group.dto.GroupUserRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServerServiceTest {

	@Mock
	private ServerRepository serverRepository;

	@Mock
	private GroupRepository groupRepository;

	@Mock
	private ServerGroupsRepository serverGroupsRepository;

	@Mock
	private ServerGroupUserRepository serverGroupUserRepository;

	@InjectMocks
	private ServerService serverService;

	@Test
	void insertServer_InsertsServerAndServerGroups() {
		String userEmail = "user-123";
		String serverName = "My Server";

		doAnswer(invocation -> {
			Server server = invocation.getArgument(0);
			server.setId(1L);
			return null;
		}).when(serverRepository).insert(any(Server.class));

		doAnswer(invocation -> {
			ServerGroup group = invocation.getArgument(0);
			group.setId(100L);
			return null;
		}).when(serverGroupsRepository).insert(any(ServerGroup.class));

		ServerDTO result = serverService.insertServer(userEmail, serverName);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals(serverName, result.getName());
		assertEquals(userEmail, result.getUserEmail());

		verify(serverRepository).insert(any(Server.class));
		verify(groupRepository).insert(anyString(), eq("main"), isNull());
		verify(serverGroupsRepository).insert(any(ServerGroup.class));
		verify(serverGroupUserRepository).insert(anyLong(), eq(userEmail));
	}

	@Test
	void getAllServersByUserId_WhenUserHasServers_ShouldReturnAllUserServers() {
		String userId = "1";

		List<Server> rows = List.of(new Server(1L, "server1", userId, "#FF004A"),
				new Server(2L, "server1", userId, "#FF004A"));

		when(serverRepository.findAllServersByUserId(userId)).thenReturn(rows);

		var result = serverService.getAll(userId);
		assertThat(result).hasSize(2);

		verify(serverRepository).findAllServersByUserId(userId);
	}

	@Test
	void deleteServer_DeleteServerAndServerGroups() {
		String userEmail = "user-123";
		String serverName = "My Server";
		Long serverIdForDelete = 1L;

		doAnswer(invocation -> {
			Server server = invocation.getArgument(0);
			server.setId(serverIdForDelete);
			return null;
		}).when(serverRepository).insert(any(Server.class));

		doAnswer(invocation -> {
			ServerGroup group = invocation.getArgument(0);
			group.setId(100L);
			return null;
		}).when(serverGroupsRepository).insert(any(ServerGroup.class));

		serverService.insertServer(userEmail, serverName);

		serverService.deleteServer(serverIdForDelete, userEmail);

		List<Server> rows = serverRepository.findAllServersByUserId(userEmail);

		assertEquals(rows.size(), 0);

		verify(groupRepository).deleteServerGroups(serverIdForDelete, userEmail);
		verify(serverRepository).deleteServer(serverIdForDelete, userEmail);
	}
}
