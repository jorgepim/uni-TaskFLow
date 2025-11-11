package com.doublevistudio.api.controller;

import com.doublevistudio.api.dto.ProyectoRequest;
import com.doublevistudio.api.dto.ResponseWrapper;
import com.doublevistudio.api.model.Proyecto;
import com.doublevistudio.api.model.Tarea;
import com.doublevistudio.api.model.Usuario;
import com.doublevistudio.api.model.UsuarioProyecto;
import com.doublevistudio.api.model.UsuarioProyectoId;
import com.doublevistudio.api.model.enums.RolProyecto;
import com.doublevistudio.api.model.Rol;
import com.doublevistudio.api.model.UsuarioRol;
import com.doublevistudio.api.repository.ProyectoRepository;
import com.doublevistudio.api.repository.TareaRepository;
import com.doublevistudio.api.repository.UsuarioProyectoRepository;
import com.doublevistudio.api.repository.UsuarioRepository;
import com.doublevistudio.api.repository.RolRepository;
import com.doublevistudio.api.repository.UsuarioRolRepository;
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

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

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

    @GetMapping(value = "/{id}/usuarios/no-asignados", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getUsuariosNoAsignados(
            @PathVariable("id") Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email
    ) {
        // verificar proyecto existe
        Optional<Proyecto> opt = proyectoRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<List<Map<String, Object>>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Proyecto no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        // obtener ids de usuarios asignados al proyecto
        List<UsuarioProyecto> asignados = usuarioProyectoRepository.findByIdProyectoId(id);
        Set<Long> asignadosIds = asignados.stream()
                .map(up -> up.getUsuario() != null ? up.getUsuario().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final String nombreFilter = nombre == null ? null : nombre.trim().toLowerCase();
        final String emailFilter = email == null ? null : email.trim().toLowerCase();

        // obtener rol USER
        Optional<Rol> rolUserOpt = rolRepository.findByNombre("USER");
        if (rolUserOpt.isEmpty()) {
            // no hay rol USER -> devolver lista vacía
            ResponseWrapper<List<Map<String, Object>>> w = new ResponseWrapper<>();
            w.setStatus("success");
            w.setMessage("Usuarios no asignados al proyecto");
            w.setTimestamp(Instant.now());
            w.setData(Collections.emptyList());
            return ResponseEntity.ok(w);
        }
        Rol rolUser = rolUserOpt.get();

        // usuarios con rol USER
        List<UsuarioRol> usuariosConRol = usuarioRolRepository.findByIdRolId(rolUser.getId());

        List<Map<String, Object>> result = usuariosConRol.stream()
                .map(UsuarioRol::getUsuario)
                .filter(Objects::nonNull)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> !asignadosIds.contains(u.getId()))
                .filter(u -> {
                    if (nombreFilter != null && (u.getNombre() == null || !u.getNombre().toLowerCase().contains(nombreFilter))) return false;
                    if (emailFilter != null && (u.getEmail() == null || !u.getEmail().toLowerCase().contains(emailFilter))) return false;
                    return true;
                })
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("nombre", u.getNombre());
                    m.put("email", u.getEmail());
                    m.put("activo", u.getActivo());
                    m.put("fecha_creacion", u.getFechaCreacion() != null ? u.getFechaCreacion().format(OUT_FMT) : null);
                    return m;
                }).collect(Collectors.toList());

        ResponseWrapper<List<Map<String, Object>>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Usuarios no asignados al proyecto");
        wrap.setTimestamp(Instant.now());
        wrap.setData(result);
        return ResponseEntity.ok(wrap);
    }

    @PostMapping(value = "/{id}/usuarios", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> assignUserToProject(@PathVariable("id") Long id,
                                                                                     @Valid @RequestBody com.doublevistudio.api.dto.AssignUserRequest req) {
        // verificar proyecto
        Optional<Proyecto> opt = proyectoRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Proyecto no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        // verificar usuario
        Optional<Usuario> userOpt = usuarioRepository.findById(req.getUsuario_id());
        if (userOpt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Usuario no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }
        Usuario user = userOpt.get();

        // validar rol_proyecto
        RolProyecto rp;
        try {
            rp = RolProyecto.valueOf(req.getRol_proyecto());
        } catch (Exception ex) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("rol_proyecto inválido");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.badRequest().body(w);
        }

        // crear o actualizar relación usuario_proyecto
        UsuarioProyectoId upId = new UsuarioProyectoId(user.getId(), id);
        UsuarioProyecto up = usuarioProyectoRepository.findById(upId).orElseGet(() -> {
            UsuarioProyecto n = new UsuarioProyecto();
            n.setId(upId);
            n.setUsuario(user);
            n.setProyecto(opt.get());
            return n;
        });
        up.setRolProyecto(rp);
        usuarioProyectoRepository.save(up);

        // obtener roles del usuario (usuario_rol)
        List<com.doublevistudio.api.model.UsuarioRol> userRoles = usuarioRolRepository.findByIdUsuarioId(user.getId());
        List<Map<String, Object>> rolesList = userRoles.stream().map(ur -> {
            Map<String, Object> rm = new HashMap<>();
            rm.put("id", ur.getRol().getId());
            rm.put("nombre", ur.getRol().getNombre());
            return rm;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("nombre", user.getNombre());
        data.put("email", user.getEmail());
        data.put("activo", user.getActivo());
        data.put("fecha_creacion", user.getFechaCreacion() != null ? user.getFechaCreacion().format(OUT_FMT) : null);
        data.put("roles", rolesList);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Usuario asignado al proyecto");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }
}
