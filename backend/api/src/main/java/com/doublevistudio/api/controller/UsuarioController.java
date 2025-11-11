package com.doublevistudio.api.controller;

import com.doublevistudio.api.dto.ResponseWrapper;
import com.doublevistudio.api.model.Proyecto;
import com.doublevistudio.api.model.UsuarioProyecto;
import com.doublevistudio.api.repository.UsuarioProyectoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioProyectoRepository usuarioProyectoRepository;

    private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping(value = "/me/proyectos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<List<Object>>> getMyProjects(
            HttpServletRequest request,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha_inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha_fin
    ) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<List<Object>> wrap = new ResponseWrapper<>();
            wrap.setStatus("error");
            wrap.setMessage("No autorizado");
            wrap.setTimestamp(Instant.now());
            wrap.setData(null);
            return ResponseEntity.status(401).body(wrap);
        }

        Long userId = Long.valueOf(String.valueOf(attr));

        List<UsuarioProyecto> ups = usuarioProyectoRepository.findByIdUsuarioId(userId);

        List<Object> results = ups.stream().map(up -> {
            Proyecto p = up.getProyecto();
            boolean matches = true;
            if (titulo != null && !titulo.isEmpty()) {
                matches = p.getTitulo() != null && p.getTitulo().toLowerCase().contains(titulo.toLowerCase());
            }
            if (matches && fecha_inicio != null) {
                matches = p.getFechaCreacion() != null && !p.getFechaCreacion().toLocalDate().isBefore(fecha_inicio);
            }
            if (matches && fecha_fin != null) {
                matches = p.getFechaCreacion() != null && !p.getFechaCreacion().toLocalDate().isAfter(fecha_fin);
            }

            if (!matches) return null;

            // Build response object for project item
            return new java.util.HashMap<String, Object>() {{
                put("id", p.getId());
                put("titulo", p.getTitulo());
                put("descripcion", p.getDescripcion());
                put("fecha_creacion", p.getFechaCreacion() != null ? p.getFechaCreacion().format(OUT_FMT) : null);
                put("creado_por", p.getCreadoPor());
                put("rol_proyecto", up.getRolProyecto().name());
            }};
        }).filter(x -> x != null).collect(Collectors.toList());

        ResponseWrapper<List<Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Proyectos del usuario");
        wrap.setTimestamp(Instant.now());
        wrap.setData(results);
        return ResponseEntity.ok(wrap);
    }
}
