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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupWithUsersDTO {
    private String id;
    private String name;
    private String image;
    private List<UserDTO> users;
}
