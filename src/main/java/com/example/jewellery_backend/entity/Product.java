package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

/**
 * Product entity mapped to 'products' table.
 * Uses Lombok to reduce boilerplate.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "markup_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal markupPercentage = BigDecimal.ZERO;

    @Column(name = "weight", precision = 8, scale = 3)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 5;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "featured")
    private Boolean featured = false;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP")
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;

    // --- relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;
}
