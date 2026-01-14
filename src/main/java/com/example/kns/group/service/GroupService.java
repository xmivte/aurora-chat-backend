package com.example.kns.group.service;

import com.example.kns.group.dto.GroupDTO;
import com.example.kns.group.dto.GroupWithUsersDTO;
import com.example.kns.group.model.Group;
import com.example.kns.group.repository.GroupRepository;
import com.example.kns.user.dto.UserDTO;
import com.example.kns.user.repository.UserRepository;
import com.example.kns.user_groups.repository.UserGroupRepository;
import com.example.kns.group.dto.GroupUserRow;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class GroupService {

	private final GroupRepository mapper;
	private final UserGroupRepository userGroupRepository;

	public List<Group> getAll(@NotBlank String userId) {
		return mapper.findAllGroupsByUserId(userId);
	}

	public List<GroupWithUsersDTO> getAllWithUsers(@NotBlank String userId) {
		List<GroupUserRow> rows = mapper.findGroupsWithUsers(userId);

		return rows.stream().collect(Collectors.groupingBy(GroupUserRow::getGroupId)).entrySet().stream().map(entry -> {
			var groupRows = entry.getValue();
			var first = groupRows.get(0);

			List<UserDTO> users = groupRows.stream().map(
					r -> UserDTO.builder().id(r.getUserId()).username(r.getUsername()).image(r.getUserImage()).build())
					.toList();

			return GroupWithUsersDTO.builder().id(first.getGroupId()).name(first.getGroupName())
					.image(first.getGroupImage()).users(users).build();
		}).toList();
	}

	public GroupDTO createGroup(@NotBlank String myUserId, @NotBlank String otherUserId) {
		Group group = new Group();
		group.setId(UUID.randomUUID().toString());
		group.setName("Group Chat");
		group.setImage(null);
		mapper.insert(group.getId(), group.getName(), group.getImage());
		userGroupRepository.insertMany(List.of(myUserId, otherUserId), group.getId());

		return GroupDTO.builder().id(group.getId()).name(group.getName()).image(group.getImage()).build();
	}
}
