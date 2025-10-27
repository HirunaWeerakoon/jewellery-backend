package com.example.jewellery_backend.controller;

import com.example.jewellery_backend.dto.*;
import com.example.jewellery_backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService service;

    @PostMapping("/register")
    public ResponseEntity<AdminUserResponse> register(@RequestBody AdminUserRegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AdminUserLoginResponse> login(@RequestBody AdminUserLoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
