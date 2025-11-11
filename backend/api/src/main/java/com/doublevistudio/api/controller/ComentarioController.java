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
}
