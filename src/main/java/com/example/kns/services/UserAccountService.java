package com.example.kns.services;

import com.example.kns.dto.UserContext;
import com.example.kns.dto.UserDataDto;
import com.example.kns.repositories.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.Integer;
import java.util.Random;

import static org.flywaydb.core.internal.util.StringUtils.leftPad;

@Service
@AllArgsConstructor
public class UserAccountService {
	private static final int SALT_LENGTH = 4;
	private static final int MAX_TRIES = 20;
	private final UserAccountRepository userRepo;
	private final Random random = new Random();

	public UserDataDto getUser(UserContext userContext) {
		var usersData = userRepo.findByUserId(userContext);

		if (usersData.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		return usersData.get();
	}

	public UserDataDto createUser(UserContext userContext, UserDataDto usersDataDto) {
		final var uniqueUsername = createUniqueUsername(usersDataDto.getUsername());
		usersDataDto.setUsername(uniqueUsername);

		userRepo.save(userContext, usersDataDto);
		return usersDataDto;
	}

	private String createUniqueUsername(String userEnteredUsername) {
		var stringBuilder = new StringBuilder(userEnteredUsername);

		for (int i = 0; i < MAX_TRIES; i++) {
			int randomInt = random.nextInt(Integer.MAX_VALUE);
			String randomSalt = String.valueOf((int) (randomInt % Math.pow(10, SALT_LENGTH)));
			randomSalt = leftPad(randomSalt, 4, '0');

			stringBuilder.append('#').append(randomSalt);
			int usersWithSameUsername = userRepo.countUsersByUsername(stringBuilder.toString());

			if (usersWithSameUsername == 0) {
				return stringBuilder.toString();
			}

			stringBuilder.delete(stringBuilder.length() - 1 - SALT_LENGTH, stringBuilder.length());
		}

		return stringBuilder.toString();
	}
}