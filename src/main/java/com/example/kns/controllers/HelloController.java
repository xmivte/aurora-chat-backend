package com.example.kns.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.kns.services.HelloService;

@RestController
@AllArgsConstructor
public class HelloController {

	private final HelloService service;

	@GetMapping("/hello")
	public String sayHello() {
		return "Hello from Spring Boot!";
	}

	@GetMapping("/test")
	public String greeting() {
		return service.greet();
	}
}
