package com.doublevistudio.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ComentarioRequest {
    @NotBlank
    private String texto;

    @NotNull
    private Long tarea_id;
}

