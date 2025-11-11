package com.doublevistudio.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String nombre;
    private String email;
    private Boolean activo;
    private LocalDateTime fecha_creacion;
    private List<RoleDTO> roles;
}

