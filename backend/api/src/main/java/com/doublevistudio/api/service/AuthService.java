package com.doublevistudio.api.service;

import com.doublevistudio.api.dto.*;
import com.doublevistudio.api.model.Rol;
import com.doublevistudio.api.model.Usuario;
import com.doublevistudio.api.model.UsuarioRol;
import com.doublevistudio.api.model.UsuarioRolId;
import com.doublevistudio.api.repository.RolRepository;
import com.doublevistudio.api.repository.UsuarioRepository;
import com.doublevistudio.api.repository.UsuarioRolRepository;
import com.doublevistudio.api.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public ResponseWrapper<AuthResponse> register(RegisterRequest req) {
        // verificar si email existe
        Optional<Usuario> exists = usuarioRepository.findByEmail(req.getEmail());
        if (exists.isPresent()) {
            ResponseWrapper<AuthResponse> wrap = new ResponseWrapper<>();
            wrap.setStatus("error");
            wrap.setMessage("El email ya está registrado");
            wrap.setTimestamp(Instant.now());
            wrap.setData(null);
            return wrap;
        }

        Usuario u = Usuario.builder()
                .nombre(req.getNombre())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .activo(req.getActivo() == null || req.getActivo())
                .build();

        Usuario saved = usuarioRepository.save(u);

        // asignar rol USER por defecto (id 2 según tu SQL)
        Rol rolUser = rolRepository.findByNombre("USER").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("USER");
            return rolRepository.save(r);
        });

        UsuarioRol ur = UsuarioRol.builder()
                .id(new UsuarioRolId(saved.getId(), rolUser.getId()))
                .usuario(saved)
                .rol(rolUser)
                .build();

        usuarioRolRepository.save(ur);

        // preparar response
        UserDTO userDTO = new UserDTO();
        userDTO.setId(saved.getId());
        userDTO.setNombre(saved.getNombre());
        userDTO.setEmail(saved.getEmail());
        userDTO.setActivo(saved.getActivo());
        userDTO.setFecha_creacion(saved.getFechaCreacion());
        RoleDTO rdto = new RoleDTO();
        rdto.setId(rolUser.getId());
        rdto.setNombre(rolUser.getNombre());
        userDTO.setRoles(Collections.singletonList(rdto));

        List<String> roles = Collections.singletonList(rolUser.getNombre());
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), roles);

        AuthResponse auth = new AuthResponse();
        auth.setUser(userDTO);
        auth.setToken(token);
        auth.setRoles(roles);

        ResponseWrapper<AuthResponse> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Usuario registrado");
        wrap.setTimestamp(Instant.now());
        wrap.setData(auth);
        return wrap;
    }

    public ResponseWrapper<AuthResponse> login(LoginRequest req) {
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            ResponseWrapper<AuthResponse> wrap = new ResponseWrapper<>();
            wrap.setStatus("error");
            wrap.setMessage("Credenciales inválidas");
            wrap.setTimestamp(Instant.now());
            wrap.setData(null);
            return wrap;
        }

        Usuario user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            ResponseWrapper<AuthResponse> wrap = new ResponseWrapper<>();
            wrap.setStatus("error");
            wrap.setMessage("Credenciales inválidas");
            wrap.setTimestamp(Instant.now());
            wrap.setData(null);
            return wrap;
        }

        // obtener roles
        List<UsuarioRol> urs = usuarioRolRepository.findByIdUsuarioId(user.getId());
        List<RoleDTO> roleDTOs = urs.stream().map(ur -> {
            RoleDTO r = new RoleDTO();
            r.setId(ur.getRol().getId());
            r.setNombre(ur.getRol().getNombre());
            return r;
        }).collect(java.util.stream.Collectors.toList());

        List<String> roles = roleDTOs.stream().map(RoleDTO::getNombre).collect(java.util.stream.Collectors.toList());
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), roles);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setNombre(user.getNombre());
        userDTO.setEmail(user.getEmail());
        userDTO.setActivo(user.getActivo());
        userDTO.setFecha_creacion(user.getFechaCreacion());
        userDTO.setRoles(roleDTOs);

        AuthResponse auth = new AuthResponse();
        auth.setUser(userDTO);
        auth.setToken(token);
        auth.setRoles(roles);

        ResponseWrapper<AuthResponse> wrap = new ResponseWrapper<>();
        wrap.setStatus("success");
        wrap.setMessage("Login exitoso");
        wrap.setTimestamp(Instant.now());
        wrap.setData(auth);
        return wrap;
    }
}
