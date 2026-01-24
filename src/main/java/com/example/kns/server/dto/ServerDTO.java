package com.example.kns.server.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class ServerDTO {
	private Long id;
	private String name;
	private String userId;
	private String backgroundColorHex;
}
