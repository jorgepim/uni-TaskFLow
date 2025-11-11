package com.doublevistudio.api.controller;

import com.doublevistudio.api.dto.AuthResponse;
import com.doublevistudio.api.dto.ResponseWrapper;
import com.doublevistudio.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<AuthResponse>> register(@Valid @RequestBody com.doublevistudio.api.dto.RegisterRequest req) {
        ResponseWrapper<AuthResponse> res = authService.register(req);
        if ("error".equals(res.getStatus())) {
            return ResponseEntity.badRequest().body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<AuthResponse>> login(@Valid @RequestBody com.doublevistudio.api.dto.LoginRequest req) {
        ResponseWrapper<AuthResponse> res = authService.login(req);
        if ("error".equals(res.getStatus())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }
        return ResponseEntity.ok(res);
    }
}
