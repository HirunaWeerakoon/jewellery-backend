package com.example.jewellery_backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for creating a product.
 * Accepts a list of CategoryDto objects to associate categories with the product.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUpdateProductRequest {
    private Long productId;                 // maps to Product.productId
    private String productName;             // maps to Product.productName
    private String sku;                     // maps to Product.sku
    private String description;             // maps to Product.description

    private BigDecimal basePrice;           // maps to Product.basePrice
    private BigDecimal markupPercentage;    // maps to Product.markupPercentage
    private BigDecimal weight;              // maps to Product.weight
    private String dimensions;              // maps to Product.dimensions

    private Integer stockQuantity;          // maps to Product.stockQuantity
    private Integer minStockLevel;          // maps to Product.minStockLevel

    private Boolean isActive;               // maps to Product.isActive
    private Boolean featured;               // maps to Product.featured

    private Boolean isGold;                 // maps to Product.isGold
    private BigDecimal goldWeightGrams;     // maps to Product.goldWeightGrams
    private Integer goldPurityKarat;        // maps to Product.goldPurityKarat

    // Related collections - use DTOs (implement these DTOs if not present)
    private List<ProductImageDto> images;
    private List<ProductCategoryDto> productCategories;
    private List<ProductAttributeValueDto> attributeValues;

    public Set<Long> getCategoryIds() {
        if (productCategories == null || productCategories.isEmpty()) {
            return Collections.emptySet();
        }
        return productCategories.stream()
                .map(ProductCategoryDto::getCategoryId)
                .collect(Collectors.toSet());
    }
}
