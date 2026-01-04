package com.example.kns.group.dto;

import lombok.*;
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
