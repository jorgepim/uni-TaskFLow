package com.doublevistudio.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class TareaCreateRequest {
    @NotBlank
    private String titulo;
    private String descripcion;
    private String fecha_vencimiento; // YYYY-MM-DD
    private String estado; // PENDIENTE, PROGRESO, COMPLETADA
    @NotNull
    private Long proyecto_id;
    private Long asignado_a;
}

