package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPostDTO {
    private String username;
    private String password;
    private String email;
}
