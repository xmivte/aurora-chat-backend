package com.example.kns.group.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import com.example.kns.user.dto.UserDTO;

@Getter
@Setter
@NoArgsConstructor
public class ServerGroupWithUsersDTO {
	private String id;
	private String name;
	private String image;
	private Long serverId;
	private List<UserDTO> users;

	@Builder
	public ServerGroupWithUsersDTO(String id, String name, String image, Long serverId, List<UserDTO> users) {
		this.id = id;
		this.name = name;
		this.image = image;
		this.serverId = serverId;
		this.users = List.copyOf(users);
	}

	public void setUsers(List<UserDTO> users) {
		this.users = List.copyOf(users);
	}

	public List<UserDTO> getUsers() {
		return users;
	}

	public static class ServerGroupWithUsersDTOBuilder {
		public ServerGroupWithUsersDTOBuilder users(List<UserDTO> users) {
			this.users = List.copyOf(users); // defensive copy
			return this;
		}
	}

}
