package com.doublevistudio.api.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioProyectoId implements Serializable {
    private Long usuarioId;
    private Long proyectoId;
}

