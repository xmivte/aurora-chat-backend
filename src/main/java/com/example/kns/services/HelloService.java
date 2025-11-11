package com.example.kns.services;

import com.example.kns.repository.HelloRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class HelloService {

    private final HelloRepository repository;

	public String greet() {
		return "approval";
	}
}
