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
public class GroupWithUsersDTO {
	private String id;
	private String name;
	private String image;
	private List<UserDTO> users;

	@Builder
	public GroupWithUsersDTO(String id, String name, String image, List<UserDTO> users) {
		this.id = id;
		this.name = name;
		this.image = image;
		this.users = List.copyOf(users);
	}

	public void setUsers(List<UserDTO> users) {
		this.users = List.copyOf(users);
	}

	public List<UserDTO> getUsers() {
		return users;
	}

	public static class GroupWithUsersDTOBuilder {
		public GroupWithUsersDTOBuilder users(List<UserDTO> users) {
			this.users = List.copyOf(users); // defensive copy
			return this;
		}
	}

}
