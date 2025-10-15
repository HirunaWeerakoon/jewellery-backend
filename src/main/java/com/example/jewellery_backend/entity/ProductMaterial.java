package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import lombok.*;

/**
 * Junction entity representing the materials used in a product.
 */
@Entity
@Table(
        name = "product_materials",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "material_id"})}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_material_id")
    private Long productMaterialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Materials material;

    @Column(name = "quantity", precision = 8, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;
}
