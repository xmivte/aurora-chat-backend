package com.example.kns.group.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CreateGroupRequest {
    private String myUserId;
    private String otherUserId;
}
