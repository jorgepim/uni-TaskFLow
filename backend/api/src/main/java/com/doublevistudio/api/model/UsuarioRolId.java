package com.doublevistudio.api.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRolId implements Serializable {
    private Long usuarioId;
    private Long rolId;
}

