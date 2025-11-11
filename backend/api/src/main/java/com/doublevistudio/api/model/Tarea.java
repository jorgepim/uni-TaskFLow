package com.doublevistudio.api.model;

import com.doublevistudio.api.model.enums.TaskEstado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskEstado estado = TaskEstado.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_a")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario asignadoA;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = TaskEstado.PENDIENTE;
        }
    }
}
