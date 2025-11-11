package com.doublevistudio.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponse {
    private UserDTO user;
    private String token;
    private List<String> roles;
}

