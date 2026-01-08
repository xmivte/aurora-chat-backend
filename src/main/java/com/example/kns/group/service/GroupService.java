package com.example.kns.group.service;

import com.example.kns.group.model.Group;
import com.example.kns.group.repository.GroupRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class GroupService {

	private final GroupRepository mapper;

	public List<Group> getAll(String userId) {
		if (userId.isBlank()) {
			throw new IllegalArgumentException("User id is blank");
		}
		return mapper.findAllGroupsByUserId(userId);
	}
}
