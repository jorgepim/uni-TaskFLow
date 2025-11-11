package com.doublevistudio.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambioEstadoRequest {
    @NotBlank
    private String estado;
}

