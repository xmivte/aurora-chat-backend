package com.example.kns.user.service;

import com.example.kns.user.dto.UserDTO;
import com.example.kns.user.model.User;
import com.example.kns.user.repository.UserRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class UserService {

	private final UserRepository mapper;

	public List<User> getAll(String groupId) {
		if (StringUtils.isBlank(groupId)) {
			throw new IllegalArgumentException("Group id is blank");
		}
		return mapper.findAllUsersByGroupId(groupId);
	}
}
