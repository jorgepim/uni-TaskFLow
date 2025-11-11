package com.doublevistudio.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignUserRequest {
    @NotNull
    private Long usuario_id;

    @NotNull
    private String rol_proyecto;
}

