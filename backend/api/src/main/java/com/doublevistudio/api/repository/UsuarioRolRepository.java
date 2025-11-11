package com.doublevistudio.api.repository;

import com.doublevistudio.api.model.UsuarioRol;
import com.doublevistudio.api.model.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {
    List<UsuarioRol> findByIdUsuarioId(Long usuarioId);
    List<UsuarioRol> findByIdRolId(Long rolId);
}
