package com.example.kns.controllers;

import com.example.kns.entities.MockUser;
import lombok.AllArgsConstructor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.kns.services.HelloService;

import java.util.List;

@RestController
@RequiredArgsConstructor
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

	@GetMapping("mock")
	public ResponseEntity<List<MockUser>> getMockUsers() {
		var list = service.findAll();
		return ResponseEntity.ok(list);
	}

	@PostMapping("mock")
	public ResponseEntity<MockUser> insertUser(@RequestBody MockUser user) {
		service.save(user);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

    @GetMapping("secure")
    public ResponseEntity<String> securedFunction(){
        return ResponseEntity.ok("Secured function");
    }
}
