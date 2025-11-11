package com.doublevistudio.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_rol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("rolId")
    @JoinColumn(name = "rol_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rol rol;
}

