package com.doublevistudio.api.controller;

import com.doublevistudio.api.dto.ResponseWrapper;
import com.doublevistudio.api.model.Proyecto;
import com.doublevistudio.api.model.Tarea;
import com.doublevistudio.api.model.Usuario;
import com.doublevistudio.api.model.UsuarioProyecto;
import com.doublevistudio.api.model.UsuarioRol;
import com.doublevistudio.api.repository.ProyectoRepository;
import com.doublevistudio.api.repository.TareaRepository;
import com.doublevistudio.api.repository.UsuarioProyectoRepository;
import com.doublevistudio.api.repository.UsuarioRepository;
import com.doublevistudio.api.repository.UsuarioRolRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioProyectoRepository usuarioProyectoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping(value = "/proyectos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> proyectosStats(HttpServletRequest request) {
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
        List<UsuarioRol> urs = usuarioRolRepository.findByIdUsuarioId(currentUserId);
        if (urs != null) {
            for (UsuarioRol ur : urs) {
                if (ur.getRol() != null && "ADMIN".equalsIgnoreCase(ur.getRol().getNombre())) {
                    isAdmin = true;
                    break;
                }
            }
        }
        if (!isAdmin) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para ver estadísticas");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(403).body(w);
        }

        List<Proyecto> proyectos = proyectoRepository.findAll();
        List<Tarea> allTareas = tareaRepository.findAll();

        // consider only usuarios activos when counting assigned users
        // build a set of active user ids
        Set<Long> activeUserIds = usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .map(Usuario::getId)
                .collect(Collectors.toSet());

        Map<String, Integer> tasksByEstadoGlobal = new HashMap<>();
        tasksByEstadoGlobal.put("PENDIENTE", 0);
        tasksByEstadoGlobal.put("PROGRESO", 0);
        tasksByEstadoGlobal.put("COMPLETADA", 0);

        for (Tarea t : allTareas) {
            String s = t.getEstado() != null ? t.getEstado().name() : "PENDIENTE";
            tasksByEstadoGlobal.put(s, tasksByEstadoGlobal.getOrDefault(s, 0) + 1);
        }

        int totalTasks = allTareas.size();
        int totalProjects = proyectos.size();
        double percentCompletedOverall = totalTasks == 0 ? 0 : (tasksByEstadoGlobal.getOrDefault("COMPLETADA", 0) * 100.0) / totalTasks;
        percentCompletedOverall = Math.round(percentCompletedOverall * 100.0) / 100.0;

        List<Map<String, Object>> projectsList = new ArrayList<>();

        for (Proyecto p : proyectos) {
            Map<String, Object> projMap = new HashMap<>();
            projMap.put("id", p.getId());
            projMap.put("titulo", p.getTitulo());

            // tareas del proyecto
            List<Tarea> tareasProyecto = allTareas.stream()
                    .filter(t -> t.getProyecto() != null && Objects.equals(t.getProyecto().getId(), p.getId()))
                    .collect(Collectors.toList());

            Map<String, Integer> byEstado = new HashMap<>();
            for (Tarea t : tareasProyecto) {
                String s = t.getEstado() != null ? t.getEstado().name() : "PENDIENTE";
                byEstado.put(s, byEstado.getOrDefault(s, 0) + 1);
            }

            int totalTareasProyecto = tareasProyecto.size();
            double percentCompleted = totalTareasProyecto == 0 ? 0 : (byEstado.getOrDefault("COMPLETADA", 0) * 100.0) / totalTareasProyecto;
            percentCompleted = Math.round(percentCompleted * 100.0) / 100.0;

            // assigned users count (only activos)
            List<UsuarioProyecto> ups = usuarioProyectoRepository.findByIdProyectoId(p.getId());
            long assignedActiveCount = ups.stream()
                    .map(UsuarioProyecto::getUsuario)
                    .filter(Objects::nonNull)
                    .map(Usuario::getId)
                    .filter(activeUserIds::contains)
                    .distinct()
                    .count();

            projMap.put("total_tareas", totalTareasProyecto);
            projMap.put("tasks_by_estado", byEstado);
            projMap.put("percent_completed", percentCompleted);
            projMap.put("assigned_users_count", assignedActiveCount);
            projMap.put("fecha_creacion", p.getFechaCreacion() != null ? p.getFechaCreacion().format(OUT_FMT) : null);

            projectsList.add(projMap);
        }

        // top projects by completion (descending percent_completed)
        List<Map<String, Object>> topProjects = projectsList.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("percent_completed"), (Double) a.get("percent_completed")))
                .collect(Collectors.toList());

        // tasks per month (based on tarea.fechaCreacion month)
        Map<String, Integer> tasksPerMonth = new HashMap<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Tarea t : allTareas) {
            if (t.getFechaCreacion() != null) {
                String key = t.getFechaCreacion().format(monthFmt);
                tasksPerMonth.put(key, tasksPerMonth.getOrDefault(key, 0) + 1);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total_projects", totalProjects);
        data.put("total_tasks", totalTasks);
        data.put("tasks_by_estado", tasksByEstadoGlobal);
        data.put("percent_completed_overall", percentCompletedOverall);
        data.put("projects", projectsList);
        data.put("top_projects_by_completion", topProjects);
        data.put("tasks_per_month", tasksPerMonth);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Estadísticas de proyectos");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    @GetMapping(value = "/tareas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> tareasStats(HttpServletRequest request) {
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
        List<UsuarioRol> urs = usuarioRolRepository.findByIdUsuarioId(currentUserId);
        if (urs != null) {
            for (UsuarioRol ur : urs) {
                if (ur.getRol() != null && "ADMIN".equalsIgnoreCase(ur.getRol().getNombre())) {
                    isAdmin = true;
                    break;
                }
            }
        }
        if (!isAdmin) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("No tienes permisos para ver estadísticas");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(403).body(w);
        }

        List<Tarea> allTareas = tareaRepository.findAll();

        Map<String, Integer> tasksByEstado = new HashMap<>();
        tasksByEstado.put("PENDIENTE", 0);
        tasksByEstado.put("PROGRESO", 0);
        tasksByEstado.put("COMPLETADA", 0);

        int overdueTasks = 0;
        LocalDate today = LocalDate.now();
        for (Tarea t : allTareas) {
            String s = t.getEstado() != null ? t.getEstado().name() : "PENDIENTE";
            tasksByEstado.put(s, tasksByEstado.getOrDefault(s, 0) + 1);

            if (t.getFechaVencimiento() != null) {
                if (t.getFechaVencimiento().isBefore(today) && (t.getEstado() == null || !"COMPLETADA".equals(t.getEstado().name()))) {
                    overdueTasks++;
                }
            }
        }

        // performance por usuario (solo usuarios activos)
        // Incluimos solo usuarios activos que además tengan el rol USER (no ADMIN)
        List<Usuario> activeUsers = usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> {
                    List<UsuarioRol> rolesForUser = usuarioRolRepository.findByIdUsuarioId(u.getId());
                    if (rolesForUser == null) return false;
                    return rolesForUser.stream().anyMatch(r -> r.getRol() != null && "USER".equalsIgnoreCase(r.getRol().getNombre()));
                })
                .collect(Collectors.toList());

        // init stats map
        Map<Long, Map<String, Object>> perfMap = new HashMap<>();
        for (Usuario u : activeUsers) {
            Map<String, Object> m = new HashMap<>();
            m.put("usuario_id", u.getId());
            m.put("nombre", u.getNombre());
            m.put("assigned_count", 0);
            m.put("completed_count", 0);
            m.put("overdue_count", 0);
            m.put("completion_rate", 0.0);
            perfMap.put(u.getId(), m);
        }

        // aggregate tasks per assigned user (only active users counted)
        for (Tarea t : allTareas) {
            if (t.getAsignadoA() == null) continue;
            Long uid = t.getAsignadoA().getId();
            if (!perfMap.containsKey(uid)) continue; // skip inactive users
            Map<String, Object> m = perfMap.get(uid);
            int assigned = (int) m.get("assigned_count");
            assigned++;
            m.put("assigned_count", assigned);
            if (t.getEstado() != null && "COMPLETADA".equals(t.getEstado().name())) {
                int comp = (int) m.get("completed_count");
                m.put("completed_count", comp + 1);
            }
            if (t.getFechaVencimiento() != null && t.getFechaVencimiento().isBefore(today)
                    && (t.getEstado() == null || !"COMPLETADA".equals(t.getEstado().name()))) {
                int ov = (int) m.get("overdue_count");
                m.put("overdue_count", ov + 1);
            }
        }

        // compute completion_rate and build list
        List<Map<String, Object>> userPerformance = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Object>> e : perfMap.entrySet()) {
            Map<String, Object> m = e.getValue();
            int assigned = (int) m.get("assigned_count");
            int completed = (int) m.get("completed_count");
            double rate = assigned == 0 ? 0.0 : (completed * 100.0) / assigned;
            rate = Math.round(rate * 100.0) / 100.0;
            m.put("completion_rate", rate);
            userPerformance.add(m);
        }

        // top performers: users with assigned_count>0 ordered by completion_rate desc then completed_count desc
        List<Map<String, Object>> topPerformers = userPerformance.stream()
                .filter(m -> (int) m.get("assigned_count") > 0)
                .sorted((a, b) -> {
                    int cmp = Double.compare((Double) b.get("completion_rate"), (Double) a.get("completion_rate"));
                    if (cmp != 0) return cmp;
                    int cb = (int) b.get("completed_count");
                    int ca = (int) a.get("completed_count");
                    return Integer.compare(cb, ca);
                })
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("total_tasks", allTareas.size());
        data.put("tasks_by_estado", tasksByEstado);
        data.put("overdue_tasks", overdueTasks);
        data.put("user_performance", userPerformance);
        data.put("top_performers", topPerformers);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Estadísticas de tareas");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }
}
