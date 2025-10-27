package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

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
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "markup_percentage", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal markupPercentage = BigDecimal.ZERO;

    @Column(name = "weight", precision = 8, scale = 3)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 5;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "is_gold")
    @Builder.Default
    private Boolean isGold = false;

    @Column(name = "gold_weight_grams", precision = 12, scale = 4)
    private BigDecimal goldWeightGrams;

    @Column(name = "gold_purity_karat")
    private Integer goldPurityKarat;

    // product_images (1:N)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductImage> images;

    // product_categories (mapped via ProductCategory entity)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductCategory> productCategories;

    // product_attribute_values
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductAttributeValue> attributeValues;

    public BigDecimal getPrice() {
        return null;
    }
}
