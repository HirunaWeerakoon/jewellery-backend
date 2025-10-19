package com.example.jewellery_backend.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AdminUserRegisterRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role; // "super_admin", "admin", etc.
    private Map<String, Boolean> permissions;
}
