package com.example.kns.group.model;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
	private String id;
	private String name;
	private String image;
}