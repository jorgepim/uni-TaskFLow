package com.doublevistudio.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ComentarioUpdateRequest {
    @NotBlank
    private String texto;
}

