package com.doublevistudio.api.controller;

import com.doublevistudio.api.dto.ProyectoRequest;
import com.doublevistudio.api.dto.ResponseWrapper;
import com.doublevistudio.api.model.Proyecto;
import com.doublevistudio.api.model.Tarea;
import com.doublevistudio.api.model.Usuario;
import com.doublevistudio.api.model.UsuarioProyecto;
import com.doublevistudio.api.model.UsuarioProyectoId;
import com.doublevistudio.api.model.enums.RolProyecto;
import com.doublevistudio.api.repository.ProyectoRepository;
import com.doublevistudio.api.repository.TareaRepository;
import com.doublevistudio.api.repository.UsuarioProyectoRepository;
import com.doublevistudio.api.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private UsuarioProyectoRepository usuarioProyectoRepository;

    @Autowired
    private TareaRepository tareaRepository;

    private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> createProject(HttpServletRequest request, @Valid @RequestBody ProyectoRequest req) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No autorizado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(401).body(w);
        }

        Long userId = Long.valueOf(String.valueOf(attr));
        Usuario usuario = usuarioRepository.findById(userId).orElse(null);
        if (usuario == null) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Usuario no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        Proyecto p = new Proyecto();
        p.setTitulo(req.getTitulo());
        p.setDescripcion(req.getDescripcion());
        p.setCreadoPor(usuario.getId());
        p.setFechaCreacion(LocalDateTime.now());

        Proyecto saved = proyectoRepository.save(p);

        // asignar rol CREADOR en usuario_proyecto
        UsuarioProyecto up = new UsuarioProyecto();
        up.setId(new UsuarioProyectoId(usuario.getId(), saved.getId()));
        up.setUsuario(usuario);
        up.setProyecto(saved);
        up.setRolProyecto(RolProyecto.CREADOR);

        usuarioProyectoRepository.save(up);

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("titulo", saved.getTitulo());
        data.put("descripcion", saved.getDescripcion());
        data.put("fecha_creacion", saved.getFechaCreacion() != null ? saved.getFechaCreacion().format(OUT_FMT) : null);
        data.put("creado_por", saved.getCreadoPor());
        data.put("rol_proyecto", up.getRolProyecto() != null ? up.getRolProyecto().name() : null);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Proyecto creado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getProjectWithTasks(HttpServletRequest request, @PathVariable("id") Long id) {
        Optional<Proyecto> opt = proyectoRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Proyecto no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        Proyecto p = opt.get();

        // creado_por info
        Map<String, Object> creadoPor = null;
        if (p.getCreadoPor() != null) {
            Usuario creador = usuarioRepository.findById(p.getCreadoPor()).orElse(null);
            if (creador != null) {
                creadoPor = new HashMap<>();
                creadoPor.put("id", creador.getId());
                creadoPor.put("nombre", creador.getNombre());
            }
        }

        // rol_proyecto para el usuario logueado (si aplica)
        Object attr = request.getAttribute("currentUserId");
        String rolProyecto = null;
        if (attr != null) {
            Long currentUserId = Long.valueOf(String.valueOf(attr));
            List<UsuarioProyecto> ups = usuarioProyectoRepository.findByIdProyectoId(id);
            for (UsuarioProyecto up : ups) {
                if (up.getUsuario() != null && up.getUsuario().getId().equals(currentUserId)) {
                    rolProyecto = up.getRolProyecto() != null ? up.getRolProyecto().name() : null;
                    break;
                }
            }
        }

        // tareas
        List<Tarea> tareas = tareaRepository.findByProyectoId(id);
        List<Map<String, Object>> tareasMap = tareas.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("titulo", t.getTitulo());
            m.put("fecha_vencimiento", t.getFechaVencimiento() != null ? t.getFechaVencimiento().toString() : null);
            m.put("estado", t.getEstado() != null ? t.getEstado().name() : null);
            if (t.getAsignadoA() != null) {
                Map<String, Object> asignado = new HashMap<>();
                asignado.put("id", t.getAsignadoA().getId());
                asignado.put("nombre", t.getAsignadoA().getNombre());
                m.put("asignado", asignado);
            } else {
                m.put("asignado", null);
            }
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("titulo", p.getTitulo());
        data.put("descripcion", p.getDescripcion());
        data.put("fecha_creacion", p.getFechaCreacion() != null ? p.getFechaCreacion().format(OUT_FMT) : null);
        data.put("creado_por", creadoPor);
        data.put("rol_proyecto", rolProyecto);
        data.put("tareas", tareasMap);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Proyecto encontrado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }
}
