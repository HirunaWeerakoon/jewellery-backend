package com.example.jewellery_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserLoginResponse {
    private String username;
    private String email;
    private String role;
    private String token; // placeholder for JWT if needed
}
