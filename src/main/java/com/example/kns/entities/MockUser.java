package com.example.kns.entities;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockUser {
	private Long id;
	private String username;
	private String email;
}