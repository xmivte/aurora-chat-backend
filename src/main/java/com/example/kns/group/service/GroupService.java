package com.example.kns.group.service;

import com.example.kns.group.dto.GroupDTO;
import com.example.kns.group.model.Group;
import com.example.kns.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

	private final GroupRepository mapper;

	public List<Group> getAll(String userId) {
		if (userId.isBlank()) {
			throw new IllegalArgumentException("User id is blank");
		}
		return mapper.findAllGroupsByUserId(userId);
	}
}
