package com.doublevistudio.api.controller;

import com.doublevistudio.api.dto.ResponseWrapper;
import com.doublevistudio.api.model.Comentario;
import com.doublevistudio.api.model.Proyecto;
import com.doublevistudio.api.model.Tarea;
import com.doublevistudio.api.model.Usuario;
import com.doublevistudio.api.model.UsuarioProyecto;
import com.doublevistudio.api.repository.ComentarioRepository;
import com.doublevistudio.api.repository.TareaRepository;
import com.doublevistudio.api.repository.UsuarioProyectoRepository;
import com.doublevistudio.api.repository.UsuarioRepository;
import com.doublevistudio.api.repository.UsuarioRolRepository;
import com.doublevistudio.api.repository.RolRepository;
import com.doublevistudio.api.model.Rol;
import com.doublevistudio.api.model.UsuarioRolId;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import com.doublevistudio.api.model.UsuarioRol;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioProyectoRepository usuarioProyectoRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolRepository rolRepository;

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

    // Nuevo endpoint: obtener tareas de un usuario (siempre usando el id del token)
    @SuppressWarnings("unused")
    @GetMapping(value = "/{id}/tareas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getUserTareas(
            HttpServletRequest request,
            @PathVariable("id") Long idPath,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long proyecto_id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimiento_before
    ) {
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

        // fetch tasks where asignado_a == userId
        List<Tarea> tareas = tareaRepository.findAll().stream()
                .filter(t -> t.getAsignadoA() != null && Objects.equals(t.getAsignadoA().getId(), userId))
                .collect(Collectors.toList());

        // apply filters
        List<Tarea> filtered = tareas.stream().filter(t -> {
            boolean ok = true;
            if (estado != null && !estado.isEmpty()) {
                ok = t.getEstado() != null && t.getEstado().name().equalsIgnoreCase(estado);
            }
            if (ok && proyecto_id != null) {
                ok = t.getProyecto() != null && Objects.equals(t.getProyecto().getId(), proyecto_id);
            }
            if (ok && vencimiento_before != null) {
                ok = t.getFechaVencimiento() != null && !t.getFechaVencimiento().isAfter(vencimiento_before);
            }
            return ok;
        }).collect(Collectors.toList());

        List<Map<String, Object>> tareasResp = filtered.stream().map(t -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", t.getId());
            item.put("titulo", t.getTitulo());
            item.put("descripcion", t.getDescripcion());
            item.put("fecha_vencimiento", t.getFechaVencimiento() != null ? t.getFechaVencimiento().toString() : null);
            item.put("estado", t.getEstado() != null ? t.getEstado().name() : null);
            item.put("proyecto_id", t.getProyecto() != null ? t.getProyecto().getId() : null);

            if (t.getAsignadoA() != null) {
                Map<String, Object> asig = new HashMap<>();
                asig.put("id", t.getAsignadoA().getId());
                asig.put("nombre", t.getAsignadoA().getNombre());
                item.put("asignado", asig);
            } else {
                item.put("asignado", null);
            }

            if (t.getCreadoPor() != null) {
                Usuario creador = usuarioRepository.findById(t.getCreadoPor()).orElse(null);
                if (creador != null) {
                    Map<String, Object> cr = new HashMap<>();
                    cr.put("id", creador.getId());
                    cr.put("nombre", creador.getNombre());
                    item.put("creado_por", cr);
                } else {
                    item.put("creado_por", null);
                }
            } else {
                item.put("creado_por", null);
            }

            item.put("fecha_creacion", t.getFechaCreacion() != null ? t.getFechaCreacion().format(OUT_FMT) : null);

            List<Comentario> comentarios = comentarioRepository.findByTareaId(t.getId());
            List<Map<String, Object>> cl = comentarios.stream().map(c -> {
                Map<String, Object> cm = new HashMap<>();
                cm.put("id", c.getId());
                cm.put("texto", c.getTexto());
                cm.put("fecha", c.getFecha() != null ? c.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
                if (c.getUsuario() != null) {
                    Map<String, Object> u = new HashMap<>();
                    u.put("id", c.getUsuario().getId());
                    u.put("nombre", c.getUsuario().getNombre());
                    cm.put("usuario", u);
                } else cm.put("usuario", null);
                return cm;
            }).collect(Collectors.toList());

            item.put("comentarios", cl);
            return item;
        }).collect(Collectors.toList());

        // stats
        Map<String, Integer> counts = new HashMap<>();
        counts.put("PENDIENTE", 0);
        counts.put("PROGRESO", 0);
        counts.put("COMPLETADA", 0);
        for (Tarea t : filtered) {
            String s = t.getEstado() != null ? t.getEstado().name() : "PENDIENTE";
            counts.put(s, counts.getOrDefault(s, 0) + 1);
        }
        int total = filtered.size();
        double porcentajeCompletadas = total == 0 ? 0 : (counts.getOrDefault("COMPLETADA", 0) * 100.0) / total;
        // ejemplo de porcentaje_progreso_ponderado: asignamos PENDIENTE=0, PROGRESO=1, COMPLETADA=2 y promediamos
        double scoreSum = 0;
        for (Tarea t : filtered) {
            String s = t.getEstado() != null ? t.getEstado().name() : "PENDIENTE";
            int v = 0;
            if (s.equals("PROGRESO")) v = 1;
            if (s.equals("COMPLETADA")) v = 2;
            scoreSum += v;
        }
        double porcentajeProgresoPonderado = total == 0 ? 0 : (scoreSum / (2.0 * total)) * 100.0;

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("total", total);
        estadisticas.put("counts", counts);
        estadisticas.put("porcentaje_completadas", Math.round(porcentajeCompletadas * 100.0) / 100.0);
        estadisticas.put("porcentaje_progreso_ponderado", Math.round(porcentajeProgresoPonderado * 100.0) / 100.0);

        Map<String, Object> data = new HashMap<>();
        data.put("tareas", tareasResp);
        data.put("estadisticas", estadisticas);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Tareas asignadas obtenidas");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getUsuarioDetalle(
            HttpServletRequest request,
            @PathVariable("id") Long id
    ) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No autorizado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(401).body(w);
        }
        Long currentUserId = Long.valueOf(String.valueOf(attr));

        // comprobar permisos: solo el propio usuario o ADMIN
        boolean isOwner = Objects.equals(currentUserId, id);
        boolean isAdmin = false;
        List<com.doublevistudio.api.model.UsuarioRol> urs = usuarioRolRepository.findByIdUsuarioId(currentUserId);
        if (urs != null) {
            for (com.doublevistudio.api.model.UsuarioRol ur : urs) {
                if (ur.getRol() != null && "ADMIN".equalsIgnoreCase(ur.getRol().getNombre())) {
                    isAdmin = true;
                    break;
                }
            }
        }

        if (!isOwner && !isAdmin) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para ver este usuario");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(403).body(w);
        }

        Optional<Usuario> uOpt = usuarioRepository.findById(id);
        if (uOpt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Usuario no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }
        Usuario user = uOpt.get();

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("nombre", user.getNombre());
        data.put("email", user.getEmail());
        data.put("activo", user.getActivo());
        data.put("fecha_creacion", user.getFechaCreacion() != null ? user.getFechaCreacion().format(OUT_FMT) : null);

        List<com.doublevistudio.api.model.UsuarioRol> userRoles = usuarioRolRepository.findByIdUsuarioId(user.getId());
        List<Map<String, Object>> roles = new ArrayList<>();
        if (userRoles != null) {
            for (com.doublevistudio.api.model.UsuarioRol ur : userRoles) {
                if (ur.getRol() != null) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("id", ur.getRol().getId());
                    r.put("nombre", ur.getRol().getNombre());
                    roles.add(r);
                }
            }
        }
        data.put("roles", roles);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Usuario encontrado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> updateUsuario(
            HttpServletRequest request,
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body
    ) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No autorizado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(401).body(w);
        }
        Long currentUserId = Long.valueOf(String.valueOf(attr));

        // comprobar permisos: solo el propio usuario o ADMIN
        boolean isOwner = Objects.equals(currentUserId, id);
        boolean isAdmin = false;
        List<UsuarioRol> urs = usuarioRolRepository.findByIdUsuarioId(currentUserId);
        if (urs != null) {
            for (UsuarioRol ur : urs) {
                if (ur.getRol() != null && "ADMIN".equalsIgnoreCase(ur.getRol().getNombre())) {
                    isAdmin = true;
                    break;
                }
            }
        }

        if (!isOwner && !isAdmin) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para editar este usuario");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(w);
        }

        Optional<Usuario> uOpt = usuarioRepository.findById(id);
        if (uOpt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Usuario no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }
        Usuario user = uOpt.get();

        // aplicar cambios parciales
        if (body.containsKey("nombre")) {
            Object v = body.get("nombre");
            if (v != null) user.setNombre(String.valueOf(v));
        }
        if (body.containsKey("email")) {
            Object v = body.get("email");
            if (v != null) {
                String newEmail = String.valueOf(v).toLowerCase();
                // validar unicidad
                Optional<Usuario> byEmail = usuarioRepository.findByEmail(newEmail);
                if (byEmail.isPresent() && !Objects.equals(byEmail.get().getId(), user.getId())) {
                    ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                    w.setStatus("error");
                    w.setMessage("Email ya en uso");
                    w.setTimestamp(Instant.now());
                    w.setData(null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(w);
                }
                user.setEmail(newEmail);
            }
        }
        if (body.containsKey("password")) {
            Object v = body.get("password");
            if (v != null) {
                String raw = String.valueOf(v);
                user.setPassword(passwordEncoder.encode(raw));
            }
        }
        if (body.containsKey("activo")) {
            Object v = body.get("activo");
            if (v != null) {
                user.setActivo(Boolean.valueOf(String.valueOf(v)));
            }
        }

        Usuario saved = usuarioRepository.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("nombre", saved.getNombre());
        data.put("email", saved.getEmail());
        data.put("activo", saved.getActivo());
        data.put("fecha_creacion", saved.getFechaCreacion() != null ? saved.getFechaCreacion().format(OUT_FMT) : null);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Usuario actualizado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @PatchMapping(value = "/{id}/admin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> updateUsuarioByAdmin(
            HttpServletRequest request,
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body
    ) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No autorizado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(401).body(w);
        }
        Long currentUserId = Long.valueOf(String.valueOf(attr));

        // solo ADMIN
        boolean isAdmin = false;
        List<com.doublevistudio.api.model.UsuarioRol> urs = usuarioRolRepository.findByIdUsuarioId(currentUserId);
        if (urs != null) {
            for (com.doublevistudio.api.model.UsuarioRol ur : urs) {
                if (ur.getRol() != null && "ADMIN".equalsIgnoreCase(ur.getRol().getNombre())) {
                    isAdmin = true;
                    break;
                }
            }
        }
        if (!isAdmin) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para editar usuarios");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(403).body(w);
        }

        Optional<Usuario> uOpt = usuarioRepository.findById(id);
        if (uOpt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Usuario no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }
        Usuario user = uOpt.get();

        // aplicar cambios parciales (ADMIN)
        if (body.containsKey("nombre")) {
            Object v = body.get("nombre");
            if (v != null) user.setNombre(String.valueOf(v));
        }
        if (body.containsKey("email")) {
            Object v = body.get("email");
            if (v != null) {
                String newEmail = String.valueOf(v).toLowerCase();
                Optional<Usuario> byEmail = usuarioRepository.findByEmail(newEmail);
                if (byEmail.isPresent() && !Objects.equals(byEmail.get().getId(), user.getId())) {
                    ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
                    w.setStatus("error");
                    w.setMessage("Email ya en uso");
                    w.setTimestamp(Instant.now());
                    w.setData(null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(w);
                }
                user.setEmail(newEmail);
            }
        }
        if (body.containsKey("password")) {
            Object v = body.get("password");
            if (v != null) {
                String raw = String.valueOf(v);
                user.setPassword(passwordEncoder.encode(raw));
            }
        }
        if (body.containsKey("activo")) {
            Object v = body.get("activo");
            if (v != null) {
                boolean act = false;
                if (v instanceof Number) act = ((Number) v).intValue() != 0;
                else {
                    String sv = String.valueOf(v);
                    if ("1".equals(sv) || "true".equalsIgnoreCase(sv)) act = true;
                }
                user.setActivo(act);
            }
        }

        // manejar rol: reemplazamos roles existentes por el rol indicado
        if (body.containsKey("rol")) {
            Object v = body.get("rol");
            if (v != null) {
                String roleName = String.valueOf(v).toUpperCase();
                Rol role = rolRepository.findByNombre(roleName).orElseGet(() -> {
                    Rol r = new Rol(); r.setNombre(roleName); return rolRepository.save(r);
                });

                // eliminar roles actuales del usuario
                List<com.doublevistudio.api.model.UsuarioRol> currentRoles = usuarioRolRepository.findByIdUsuarioId(user.getId());
                if (currentRoles != null && !currentRoles.isEmpty()) {
                    usuarioRolRepository.deleteAll(currentRoles);
                }

                // asignar nuevo rol
                com.doublevistudio.api.model.UsuarioRol newUr = com.doublevistudio.api.model.UsuarioRol.builder()
                        .id(new UsuarioRolId(user.getId(), role.getId()))
                        .usuario(user)
                        .rol(role)
                        .build();
                usuarioRolRepository.save(newUr);
            }
        }

        Usuario saved = usuarioRepository.save(user);

        // preparar roles en respuesta
        List<com.doublevistudio.api.model.UsuarioRol> savedRoles = usuarioRolRepository.findByIdUsuarioId(saved.getId());
        List<Map<String, Object>> roles = new ArrayList<>();
        if (savedRoles != null) {
            for (com.doublevistudio.api.model.UsuarioRol ur : savedRoles) {
                if (ur.getRol() != null) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("id", ur.getRol().getId());
                    r.put("nombre", ur.getRol().getNombre());
                    roles.add(r);
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("nombre", saved.getNombre());
        data.put("email", saved.getEmail());
        data.put("activo", saved.getActivo());
        data.put("fecha_creacion", saved.getFechaCreacion() != null ? saved.getFechaCreacion().format(OUT_FMT) : null);
        data.put("roles", roles);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Usuario actualizado por ADMIN");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getAllUsuarios(
            HttpServletRequest request,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String rol
    ) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No autorizado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(401).body(w);
        }
        Long currentUserId = Long.valueOf(String.valueOf(attr));

        // solo ADMIN puede acceder
        boolean isAdmin = false;
        List<com.doublevistudio.api.model.UsuarioRol> urs = usuarioRolRepository.findByIdUsuarioId(currentUserId);
        if (urs != null) {
            for (com.doublevistudio.api.model.UsuarioRol ur : urs) {
                if (ur.getRol() != null && "ADMIN".equalsIgnoreCase(ur.getRol().getNombre())) {
                    isAdmin = true;
                    break;
                }
            }
        }
        if (!isAdmin) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para listar usuarios");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(403).body(w);
        }

        List<Usuario> all = usuarioRepository.findAll();
        List<Map<String, Object>> usuarios = new ArrayList<>();

        Map<String, Integer> porRol = new HashMap<>();
        int activos = 0;
        int inactivos = 0;

        for (Usuario u : all) {
            // obtener roles para el usuario
            List<com.doublevistudio.api.model.UsuarioRol> userRoles = usuarioRolRepository.findByIdUsuarioId(u.getId());
            List<String> roleNames = new ArrayList<>();
            List<Map<String, Object>> roleObjs = new ArrayList<>();
            if (userRoles != null) {
                for (com.doublevistudio.api.model.UsuarioRol ur : userRoles) {
                    if (ur.getRol() != null) {
                        String rn = ur.getRol().getNombre();
                        roleNames.add(rn);
                        roleObjs.add(new HashMap<>() {{ put("id", ur.getRol().getId()); put("nombre", rn); }});
                        porRol.put(rn, porRol.getOrDefault(rn, 0) + 1);
                    }
                }
            }

            if (Boolean.TRUE.equals(u.getActivo())) activos++; else inactivos++;

            // aplicar filtros
            boolean ok = true;
            if (nombre != null && !nombre.isEmpty()) {
                ok = u.getNombre() != null && u.getNombre().toLowerCase().contains(nombre.toLowerCase());
            }
            if (ok && email != null && !email.isEmpty()) {
                ok = u.getEmail() != null && u.getEmail().toLowerCase().contains(email.toLowerCase());
            }
            if (ok && rol != null && !rol.isEmpty()) {
                ok = roleNames.stream().anyMatch(rn -> rn.equalsIgnoreCase(rol));
            }

            if (!ok) continue;

            Map<String, Object> um = new HashMap<>();
            um.put("id", u.getId());
            um.put("nombre", u.getNombre());
            um.put("email", u.getEmail());
            um.put("activo", u.getActivo());
            um.put("fecha_creacion", u.getFechaCreacion() != null ? u.getFechaCreacion().format(OUT_FMT) : null);
            um.put("roles", roleObjs);
            usuarios.add(um);
        }

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("total", usuarios.size());
        estadisticas.put("activos", activos);
        estadisticas.put("inactivos", inactivos);
        estadisticas.put("por_rol", porRol);

        Map<String, Object> data = new HashMap<>();
        data.put("usuarios", usuarios);
        data.put("estadisticas", estadisticas);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Usuarios obtenidos");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Object>> deleteUsuarioByAdmin(
            HttpServletRequest request,
            @PathVariable("id") Long id
    ) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) {
            ResponseWrapper<Object> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No autorizado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(401).body(w);
        }
        Long currentUserId = Long.valueOf(String.valueOf(attr));

        // solo ADMIN
        boolean isAdmin = false;
        List<com.doublevistudio.api.model.UsuarioRol> urs = usuarioRolRepository.findByIdUsuarioId(currentUserId);
        if (urs != null) {
            for (com.doublevistudio.api.model.UsuarioRol ur : urs) {
                if (ur.getRol() != null && "ADMIN".equalsIgnoreCase(ur.getRol().getNombre())) {
                    isAdmin = true;
                    break;
                }
            }
        }
        if (!isAdmin) {
            ResponseWrapper<Object> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para eliminar usuarios");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(403).body(w);
        }

        Optional<Usuario> uOpt = usuarioRepository.findById(id);
        if (uOpt.isEmpty()) {
            ResponseWrapper<Object> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Usuario no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }
        Usuario user = uOpt.get();

        // comprobar asignaciones a proyectos
        List<UsuarioProyecto> asignaciones = usuarioProyectoRepository.findByIdUsuarioId(user.getId());
        if (asignaciones != null && !asignaciones.isEmpty()) {
            // marcar inactivo
            user.setActivo(false);
            usuarioRepository.save(user);

            ResponseWrapper<Object> wrap = new ResponseWrapper<>();
            wrap.setStatus("success");
            wrap.setMessage("Usuario marcado como inactivo porque está asignado a proyectos");
            wrap.setTimestamp(Instant.now());
            wrap.setData(null);
            return ResponseEntity.ok(wrap);
        }

        // si no tiene asignaciones, eliminar usuario y sus roles
        List<com.doublevistudio.api.model.UsuarioRol> currentRoles = usuarioRolRepository.findByIdUsuarioId(user.getId());
        if (currentRoles != null && !currentRoles.isEmpty()) {
            usuarioRolRepository.deleteAll(currentRoles);
        }
        usuarioRepository.delete(user);

        ResponseWrapper<Object> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Usuario eliminado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(null);
        return ResponseEntity.ok(wrap);
    }

}
