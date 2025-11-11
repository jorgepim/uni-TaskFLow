package com.doublevistudio.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProyectoRequest {
    @NotBlank
    private String titulo;

    private String descripcion;
}

