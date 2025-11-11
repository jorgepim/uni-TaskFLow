package com.doublevistudio.api.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private Boolean activo = true;
}

