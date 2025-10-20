package com.example.jewellery_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class AdminUserResponse {
    private Long adminId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Map<String, Boolean> permissions;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
