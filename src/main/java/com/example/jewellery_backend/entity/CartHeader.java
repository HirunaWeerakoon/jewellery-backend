package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

/**
 * Entity representing a shopping cart per session.
 */
@Entity
@Table(name = "cart_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_header_id")
    private Long cartHeaderId;

    @Column(name = "session_id", nullable = false, unique = true, length = 128)
    private String sessionId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;
}
