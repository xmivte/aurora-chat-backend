package com.example.kns.user.model;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	private String id;
	private String username;
	private String email;
	private String image;
}