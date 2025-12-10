package com.example.kns.user.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class User {
	private Long id;
	private String username;
	private String email;
	private String image;
}