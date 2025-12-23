package com.example.kns.user.dto;

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
public class UserDTO {
	private String id;
	private String username;
	private String image;
}
