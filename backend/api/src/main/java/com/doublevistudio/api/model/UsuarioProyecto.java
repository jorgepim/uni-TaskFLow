package com.doublevistudio.api.model;

import com.doublevistudio.api.model.enums.RolProyecto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_proyecto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioProyecto {

    @EmbeddedId
    private UsuarioProyectoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("proyectoId")
    @JoinColumn(name = "proyecto_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_proyecto", nullable = false)
    private RolProyecto rolProyecto = RolProyecto.COLABORADOR;
}

