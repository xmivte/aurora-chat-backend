package com.example.kns.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.kns.services.HelloService;

@RestController
public class HelloController {

	private final HelloService service;

	public HelloController(HelloService service) {
		this.service = service;
	}

	@GetMapping("/hello")
	public String sayHello() {
		return "Hello from Spring Boot!";
	}

	@GetMapping("/test")
	public String greeting() {
		return service.greet();
	}
}
