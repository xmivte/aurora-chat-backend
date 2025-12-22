package com.example.kns.services;

import com.example.kns.entities.MockUser;
import com.example.kns.repositories.HelloRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class HelloService {

	private final MockService mockService;
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
	private final HelloRepository repository;

	public String getText() {
		return mockService.getText();
	}

	public String greet() {
		return "approval";
	}

	public List<MockUser> findAll() {
		return repository.findAll();
	}

	public void save(MockUser user) {
		repository.save(user);
	}
}
