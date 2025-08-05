package com.example.task_manager.controller;

import com.example.task_manager.dto.AuthRequest;
import com.example.task_manager.dto.AuthResponse;
import com.example.task_manager.dto.RegisterRequest;
import com.example.task_manager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.authenticate(authRequest));
    }

    @GetMapping("/test")
    public String test() {
        return "Test endpoint";
    }
}
