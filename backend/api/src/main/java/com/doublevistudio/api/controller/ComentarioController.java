package com.doublevistudio.api.controller;

import com.doublevistudio.api.dto.ComentarioRequest;
import com.doublevistudio.api.dto.ResponseWrapper;
import com.doublevistudio.api.model.Comentario;
import com.doublevistudio.api.model.Tarea;
import com.doublevistudio.api.model.Usuario;
import com.doublevistudio.api.repository.ComentarioRepository;
import com.doublevistudio.api.repository.TareaRepository;
import com.doublevistudio.api.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> createComentario(HttpServletRequest request, @Valid @RequestBody ComentarioRequest req) {
        // obtener usuario actual desde el request (set por filtro JWT)
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

        Optional<Tarea> tareaOpt = tareaRepository.findById(req.getTarea_id());
        if (tareaOpt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Tarea no encontrada");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        Tarea tarea = tareaOpt.get();

        Comentario c = new Comentario();
        c.setTexto(req.getTexto());
        c.setTarea(tarea);
        c.setUsuario(usuario);
        // fecha se setea en prePersist si es null

        Comentario saved = comentarioRepository.save(c);

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("texto", saved.getTexto());
        // según el ejemplo se espera "fecha": null; si prefieres devolver la fecha real, cambia aquí a saved.getFecha().format(...)
        data.put("fecha", null);
        data.put("tarea_id", tarea.getId());
        data.put("usuario_id", usuario.getId());

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Comentario creado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    // PATCH endpoint para actualizar comentario (solo autor)
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> updateComentario(HttpServletRequest request,
                                                                                  @PathVariable("id") Long id,
                                                                                  @Valid @RequestBody com.doublevistudio.api.dto.ComentarioUpdateRequest req) {
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

        Optional<Comentario> opt = comentarioRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Comentario no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        Comentario c = opt.get();
        if (c.getUsuario() == null || !c.getUsuario().getId().equals(userId)) {
            ResponseWrapper<Map<String, Object>> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Solo el autor puede editar el comentario");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(403).body(w);
        }

        c.setTexto(req.getTexto());
        Comentario saved = comentarioRepository.save(c);

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("texto", saved.getTexto());
        data.put("fecha", saved.getFecha() != null ? saved.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        data.put("tarea_id", saved.getTarea() != null ? saved.getTarea().getId() : null);
        data.put("usuario_id", saved.getUsuario() != null ? saved.getUsuario().getId() : null);

        ResponseWrapper<Map<String, Object>> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Comentario actualizado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(data);
        return ResponseEntity.ok(wrap);
    }

    // DELETE endpoint para eliminar un comentario (solo autor)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<Object>> deleteComentario(HttpServletRequest request,
                                                                     @PathVariable("id") Long id) {
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

        Optional<Comentario> opt = comentarioRepository.findById(id);
        if (opt.isEmpty()) {
            ResponseWrapper<Object> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Comentario no encontrado");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(404).body(w);
        }

        Comentario c = opt.get();
        if (c.getUsuario() == null || !c.getUsuario().getId().equals(userId)) {
            ResponseWrapper<Object> w = new ResponseWrapper<>();
            w.setStatus("error");
            w.setMessage("Solo el autor puede eliminar el comentario");
            w.setTimestamp(Instant.now());
            w.setData(null);
            return ResponseEntity.status(403).body(w);
        }

        comentarioRepository.delete(c);

        ResponseWrapper<Object> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Comentario eliminado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(null);
        return ResponseEntity.ok(wrap);
    }
}
