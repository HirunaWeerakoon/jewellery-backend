package com.example.jewellery_backend.dto;

import lombok.Data;

@Data
public class AdminUserLoginRequest {
    private String username;
    private String password;
}
