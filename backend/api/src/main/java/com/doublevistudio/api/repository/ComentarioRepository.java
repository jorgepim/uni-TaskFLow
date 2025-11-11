package com.doublevistudio.api.repository;

import com.doublevistudio.api.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByTareaId(Long tareaId);
}

