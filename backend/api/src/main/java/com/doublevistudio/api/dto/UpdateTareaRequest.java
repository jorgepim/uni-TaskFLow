package com.doublevistudio.api.dto;

import lombok.Data;

@Data
public class UpdateTareaRequest {
    private String titulo;
    private String descripcion;
    private String fecha_vencimiento; // formato YYYY-MM-DD
    private Long asignado_a;
}

