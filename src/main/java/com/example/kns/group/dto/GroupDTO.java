package com.example.kns.group.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GroupDTO {
	private String id;
	private String name;
	private String image;
}
