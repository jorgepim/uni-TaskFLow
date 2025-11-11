package com.doublevistudio.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "proyectos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Tarea> tareas = new HashSet<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UsuarioProyecto> usuarios = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
