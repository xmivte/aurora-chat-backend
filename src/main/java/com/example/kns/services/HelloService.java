package com.example.kns.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelloService {

	MockService mockService;

	public String getText() {
		return mockService.getText();
	}

	public String greet() {
		return "approval";
	}
}
