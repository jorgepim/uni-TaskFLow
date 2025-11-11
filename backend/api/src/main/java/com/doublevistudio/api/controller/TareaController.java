package com.doublevistudio.api.controller;

import com.doublevistudio.api.dto.ResponseWrapper;
import com.doublevistudio.api.model.Comentario;
import com.doublevistudio.api.model.Tarea;
import com.doublevistudio.api.model.Usuario;
import com.doublevistudio.api.repository.*;
import com.doublevistudio.api.dto.CambioEstadoRequest;
import com.doublevistudio.api.dto.UpdateTareaRequest;
import com.doublevistudio.api.dto.TareaCreateRequest;
import com.doublevistudio.api.model.Proyecto;
import com.doublevistudio.api.model.enums.TaskEstado;
import com.doublevistudio.api.model.UsuarioProyecto;
import com.doublevistudio.api.model.UsuarioProyectoId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioProyectoRepository usuarioProyectoRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getTareaDetalle(@PathVariable("id") Long id) {
        Optional<Tarea> opt = tareaRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Tarea no encontrada");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        Tarea t = opt.get();

        Map<String, Object> data = new HashMap<>();
        data.put("id", t.getId());
        data.put("titulo", t.getTitulo());
        data.put("descripcion", t.getDescripcion());
        data.put("fecha_vencimiento", t.getFechaVencimiento() != null ? t.getFechaVencimiento().toString() : null);
        data.put("estado", t.getEstado() != null ? t.getEstado().name() : null);
        data.put("proyecto_id", t.getProyecto() != null ? t.getProyecto().getId() : null);

        if (t.getAsignadoA() != null) {
            Map<String, Object> asignado = new HashMap<>();
            asignado.put("id", t.getAsignadoA().getId());
            asignado.put("nombre", t.getAsignadoA().getNombre());
            data.put("asignado", asignado);
        } else {
            data.put("asignado", null);
        }

        // creado_por info
        if (t.getCreadoPor() != null) {
            Usuario creador = usuarioRepository.findById(t.getCreadoPor()).orElse(null);
            if (creador != null) {
                Map<String, Object> creadoPor = new HashMap<>();
                creadoPor.put("id", creador.getId());
                creadoPor.put("nombre", creador.getNombre());
                data.put("creado_por", creadoPor);
            } else {
                data.put("creado_por", null);
            }
        } else {
            data.put("creado_por", null);
        }

        data.put("fecha_creacion", t.getFechaCreacion() != null ? t.getFechaCreacion().format(OUT_FMT) : null);

        // comentarios
        List<Comentario> comentarios = comentarioRepository.findByTareaId(t.getId());
        List<Map<String, Object>> comentList = comentarios.stream().map(c -> {
            Map<String, Object> cm = new HashMap<>();
            cm.put("id", c.getId());
            cm.put("texto", c.getTexto());
            cm.put("fecha", c.getFecha() != null ? c.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            if (c.getUsuario() != null) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", c.getUsuario().getId());
                u.put("nombre", c.getUsuario().getNombre());
                cm.put("usuario", u);
            } else {
                cm.put("usuario", null);
            }
            return cm;
        }).collect(Collectors.toList());

        data.put("comentarios", comentList);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Tarea encontrada");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @PatchMapping(value = "/{id}/estado", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> changeEstado(HttpServletRequest request, @PathVariable("id") Long id, @Valid @RequestBody CambioEstadoRequest req) {
        Optional<Tarea> opt = tareaRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Tarea no encontrada");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        Tarea t = opt.get();

        // obtener usuario actual
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

        boolean permitido = (t.getAsignadoA() != null && t.getAsignadoA().getId().equals(userId));

        // si tiene rol CREADOR en el proyecto
        if (!permitido) {
            Long proyectoId = t.getProyecto() != null ? t.getProyecto().getId() : null;
            if (proyectoId != null) {
                UsuarioProyecto up = usuarioProyectoRepository.findById(new UsuarioProyectoId(userId, proyectoId)).orElse(null);
                if (up != null && up.getRolProyecto() != null && up.getRolProyecto().name().equals("CREADOR")) {
                    permitido = true;
                }
            }
        }

        if (!permitido) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para cambiar el estado de la tarea");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(w);
        }

        // validar estado
        TaskEstado nuevoEstado;
        try {
            nuevoEstado = TaskEstado.valueOf(req.getEstado());
        } catch (Exception ex) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Estado inválido");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.badRequest().body(w);
        }

        t.setEstado(nuevoEstado);
        tareaRepository.save(t);

        Map<String, Object> data = new HashMap<>();
        data.put("id", t.getId());
        data.put("titulo", t.getTitulo());
        data.put("descripcion", t.getDescripcion());
        data.put("fecha_vencimiento", t.getFechaVencimiento() != null ? t.getFechaVencimiento().toString() : null);
        data.put("estado", t.getEstado() != null ? t.getEstado().name() : null);
        data.put("proyecto_id", t.getProyecto() != null ? t.getProyecto().getId() : null);
        data.put("fecha_creacion", t.getFechaCreacion() != null ? t.getFechaCreacion().format(OUT_FMT) : null);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Estado de tarea actualizado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> updateTarea(HttpServletRequest request, @PathVariable("id") Long id, @RequestBody UpdateTareaRequest req) {
        Optional<Tarea> opt = tareaRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Tarea no encontrada");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        Tarea t = opt.get();

        // obtener usuario actual
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

        boolean permitido = (t.getAsignadoA() != null && t.getAsignadoA().getId().equals(userId));
        if (!permitido) {
            Long proyectoId = t.getProyecto() != null ? t.getProyecto().getId() : null;
            if (proyectoId != null) {
                UsuarioProyecto up = usuarioProyectoRepository.findById(new UsuarioProyectoId(userId, proyectoId)).orElse(null);
                if (up != null && up.getRolProyecto() != null && up.getRolProyecto().name().equals("CREADOR")) {
                    permitido = true;
                }
            }
        }

        if (!permitido) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para editar la tarea");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(w);
        }

        // aplicar cambios parciales
        if (req.getTitulo() != null) t.setTitulo(req.getTitulo());
        if (req.getDescripcion() != null) t.setDescripcion(req.getDescripcion());
        if (req.getFecha_vencimiento() != null) {
            try {
                LocalDate fecha = LocalDate.parse(req.getFecha_vencimiento());
                t.setFechaVencimiento(fecha);
            } catch (Exception ex) {
                ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                w.setStatus("error");
                w.setMessage("fecha_vencimiento inválida, usar YYYY-MM-DD");
                w.setTimestamp(Instant.now());
                w.setData(null);
                return ResponseEntity.badRequest().body(w);
            }
        }
        if (req.getAsignado_a() != null) {
            Optional<Usuario> uOpt = usuarioRepository.findById(req.getAsignado_a());
            if (uOpt.isEmpty()) {
                ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                w.setStatus("error");
                w.setMessage("Usuario asignado no encontrado");
                w.setTimestamp(Instant.now());
                w.setData(null);
                return ResponseEntity.status(404).body(w);
            }
            Usuario u = uOpt.get();
            if (!Boolean.TRUE.equals(u.getActivo())) {
                ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                w.setStatus("error");
                w.setMessage("Usuario no activo");
                w.setTimestamp(Instant.now());
                w.setData(null);
                return ResponseEntity.badRequest().body(w);
            }
            t.setAsignadoA(u);
        }

        tareaRepository.save(t);

        Map<String, Object> data = new HashMap<>();
        data.put("id", t.getId());
        data.put("titulo", t.getTitulo());
        data.put("descripcion", t.getDescripcion());
        data.put("fecha_vencimiento", t.getFechaVencimiento() != null ? t.getFechaVencimiento().toString() : null);
        data.put("estado", t.getEstado() != null ? t.getEstado().name() : null);
        data.put("proyecto_id", t.getProyecto() != null ? t.getProyecto().getId() : null);
        data.put("fecha_creacion", t.getFechaCreacion() != null ? t.getFechaCreacion().format(OUT_FMT) : null);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Tarea actualizada");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> createTarea(HttpServletRequest request, @Valid @RequestBody TareaCreateRequest req) {
        // usuario creador
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No autorizado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(401).body(w);
        }
        Long creadorId = Long.valueOf(String.valueOf(attr));

        // validar proyecto
        Optional<Proyecto> proyectoOpt = proyectoRepository.findById(req.getProyecto_id());
        if (proyectoOpt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Proyecto no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }
        Proyecto proyecto = proyectoOpt.get();

        // validar usuario asignado si viene
        Usuario asignado = null;
        if (req.getAsignado_a() != null) {
            Optional<Usuario> uOpt = usuarioRepository.findById(req.getAsignado_a());
            if (uOpt.isEmpty()) {
                ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                w.setStatus("error");
                w.setMessage("Usuario asignado no encontrado");
                w.setTimestamp(Instant.now());
                w.setData(null);
                return ResponseEntity.status(404).body(w);
            }
            asignado = uOpt.get();
            if (!Boolean.TRUE.equals(asignado.getActivo())) {
                ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                w.setStatus("error");
                w.setMessage("Usuario asignado no activo");
                w.setTimestamp(Instant.now());
                w.setData(null);
                return ResponseEntity.badRequest().body(w);
            }
        }

        // validar estado
        TaskEstado estado;
        if (req.getEstado() == null) {
            estado = TaskEstado.PENDIENTE;
        } else {
            try {
                estado = TaskEstado.valueOf(req.getEstado());
            } catch (Exception ex) {
                ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                w.setStatus("error");
                w.setMessage("Estado inválido");
                w.setTimestamp(Instant.now());
                w.setData(null);
                return ResponseEntity.badRequest().body(w);
            }
        }

        Tarea t = new Tarea();
        t.setTitulo(req.getTitulo());
        t.setDescripcion(req.getDescripcion());
        if (req.getFecha_vencimiento() != null) {
            try {
                t.setFechaVencimiento(LocalDate.parse(req.getFecha_vencimiento()));
            } catch (Exception ex) {
                ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                w.setStatus("error");
                w.setMessage("fecha_vencimiento inválida, usar YYYY-MM-DD");
                w.setTimestamp(Instant.now());
                w.setData(null);
                return ResponseEntity.badRequest().body(w);
            }
        }
        t.setEstado(estado);
        t.setProyecto(proyecto);
        t.setAsignadoA(asignado);
        t.setCreadoPor(creadorId);
        // fechaCreacion se setea en @PrePersist

        Tarea saved = tareaRepository.save(t);

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("titulo", saved.getTitulo());
        data.put("descripcion", saved.getDescripcion());
        data.put("fecha_vencimiento", saved.getFechaVencimiento() != null ? saved.getFechaVencimiento().toString() : null);
        data.put("estado", saved.getEstado() != null ? saved.getEstado().name() : null);
        data.put("proyecto_id", saved.getProyecto() != null ? saved.getProyecto().getId() : null);
        data.put("asignado_a", saved.getAsignadoA() != null ? saved.getAsignadoA().getId() : null);
        data.put("creado_por", saved.getCreadoPor());
        data.put("fecha_creacion", saved.getFechaCreacion() != null ? saved.getFechaCreacion().format(OUT_FMT) : null);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Tarea creada");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    // Eliminar tarea y sus comentarios
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<ResponseWrapper<Object>> deleteTarea(HttpServletRequest request, @PathVariable("id") Long id) {
        Optional<Tarea> opt = tareaRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<Object> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Tarea no encontrada");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }
        Tarea t = opt.get();

        // obtener usuario actual
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<Object> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No autorizado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(401).body(w);
        }
        Long userId = Long.valueOf(String.valueOf(attr));

        boolean permitido = (t.getAsignadoA() != null && t.getAsignadoA().getId().equals(userId));
        if (!permitido) {
            Long proyectoId = t.getProyecto() != null ? t.getProyecto().getId() : null;
            if (proyectoId != null) {
                UsuarioProyecto up = usuarioProyectoRepository.findById(new UsuarioProyectoId(userId, proyectoId)).orElse(null);
                if (up != null && up.getRolProyecto() != null && up.getRolProyecto().name().equals("CREADOR")) {
                    permitido = true;
                }
            }
        }

        if (!permitido) {
            ResponseWrapper<Object> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para eliminar la tarea");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(w);
        }

        // eliminar comentarios relacionados
        List<Comentario> comentarios = comentarioRepository.findByTareaId(t.getId());
        if (comentarios != null && !comentarios.isEmpty()) {
            comentarioRepository.deleteAll(comentarios);
        }

        // eliminar tarea
        tareaRepository.delete(t);

        ResponseWrapper<Object> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Tarea eliminada");
        wrap.setTimestamp(Instant.now());
        wrap.setData(null);
        return ResponseEntity.ok(wrap);
    }

}
