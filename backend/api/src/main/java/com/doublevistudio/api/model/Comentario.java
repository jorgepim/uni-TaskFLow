package com.doublevistudio.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text", nullable = false)
    private String texto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
