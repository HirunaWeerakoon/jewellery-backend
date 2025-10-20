package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.*;
import org.hibernate.annotations.Type;

/**
 * Entity representing admin users in the system.
 */
@Entity
@Table(name = "admin_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.staff;

    @Type(JsonType.class)
    @Column(name = "permissions", columnDefinition = "json")
    private Map<String, Boolean> permissions; // example: {"canEdit": true, "canDelete": false}

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- Enum for roles ---
    public enum Role {
        super_admin,
        admin,
        manager,
        staff
    }
}
