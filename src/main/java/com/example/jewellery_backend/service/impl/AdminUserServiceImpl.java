package com.example.jewellery_backend.service.impl;

import com.example.jewellery_backend.dto.*;
import com.example.jewellery_backend.entity.AdminUser;
import com.example.jewellery_backend.exception.ResourceNotFoundException;
import com.example.jewellery_backend.repository.AdminUserRepository;
import com.example.jewellery_backend.service.AdminUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminUserResponse register(AdminUserRegisterRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        AdminUser user = AdminUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(AdminUser.Role.valueOf(request.getRole()))
                .permissions(request.getPermissions())
                .isActive(true)
                .build();

        AdminUser saved = repository.save(user);

        return mapToResponse(saved);
    }

    @Override
    public AdminUserResponse login(AdminUserLoginRequest request) { // <<< Return AdminUserResponse
        AdminUser user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername())); // Use better exception

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Consider throwing a more specific AuthenticationException if integrating deeper later
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Update last login time
        user.setLastLogin(LocalDateTime.now());
        repository.save(user);

        // Return user details without a token
        return mapToResponse(user); // <<< Use existing mapper
    }

    @Override
    public AdminUserResponse getById(Long id) {
        AdminUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    private AdminUserResponse mapToResponse(AdminUser user) {
        return AdminUserResponse.builder()
                .adminId(user.getAdminId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .permissions(user.getPermissions())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
