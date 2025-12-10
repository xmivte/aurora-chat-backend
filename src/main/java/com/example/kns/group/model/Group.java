package com.example.kns.group.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class Group {
	private String id;
	private String name;
	private String image;
}