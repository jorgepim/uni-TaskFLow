package com.doublevistudio.api.repository;

import com.doublevistudio.api.model.UsuarioProyecto;
import com.doublevistudio.api.model.UsuarioProyectoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioProyectoRepository extends JpaRepository<UsuarioProyecto, UsuarioProyectoId> {
    List<UsuarioProyecto> findByIdProyectoId(Long proyectoId);
    List<UsuarioProyecto> findByIdUsuarioId(Long usuarioId);
}

