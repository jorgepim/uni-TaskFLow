package com.doublevistudio.api.dto;

import lombok.Data;

@Data
public class ProyectoUpdateRequest {
    private String titulo;
    private String descripcion;
    // formato esperado: yyyy-MM-dd (opcional)
    private String fecha_creacion;
}
