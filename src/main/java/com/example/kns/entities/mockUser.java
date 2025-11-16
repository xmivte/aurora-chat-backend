package com.example.kns.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mock_users")
@Data
public class mockUser
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
}
