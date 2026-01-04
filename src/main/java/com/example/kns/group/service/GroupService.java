package com.example.kns.group.service;

import com.example.kns.group.dto.GroupDTO;
import com.example.kns.group.dto.GroupWithUsersDTO;
import com.example.kns.group.model.Group;
import com.example.kns.group.repository.GroupRepository;
import com.example.kns.user.dto.UserDTO;
import com.example.kns.user.repository.UserRepository;
import com.example.kns.user_groups.repository.UserGroupRepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class GroupService {

	private final GroupRepository mapper;
	private final UserRepository userRepository;
	private final UserGroupRepository userGroupRepository;

	public List<Group> getAll(String userId) {
	 if (userId.isBlank()) {
	 throw new IllegalArgumentException("User id is blank");
	 }
	 return mapper.findAllGroupsByUserId(userId);
	 }

	public List<GroupWithUsersDTO> getAllWithUsers(String userId) {
		if (userId.isBlank()) {
			throw new IllegalArgumentException("User id is blank");
		}

		List<Group> groups = mapper.findAllGroupsByUserId(userId);

		return groups.stream()
				.map(group -> {
					List<UserDTO> users = userRepository.findAllUsersByGroupId(group.getId())
							.stream()
							.map(u -> UserDTO.builder()
									.id(u.getId())
									.username(u.getUsername())
									.image(u.getImage())
									.build())
							.toList();

					return GroupWithUsersDTO.builder()
							.id(group.getId())
							.name(group.getName())
							.image(group.getImage())
							.users(users)
							.build();
				})
				.toList();
	}


	public GroupDTO createGroup(String myUserId, String otherUserId) { 
		Group group = new Group();
		group.setId(UUID.randomUUID().toString());
		group.setName("Group Chat");
		group.setImage(null);
		mapper.insert(group.getId(), group.getName(), group.getImage());
		userGroupRepository.insert(myUserId, group.getId());
		userGroupRepository.insert(otherUserId, group.getId());

		return GroupDTO.builder().id(group.getId()).name(group.getName()).image(group.getImage()).build();
	}
}
