package com.example.jewellery_backend.dto;

import com.example.jewellery_backend.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object (DTO) for Product entity.
 * Represents a lightweight version of Product used in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long productId;
    private String productName;
    private String sku;
    private String description;

    private BigDecimal basePrice;
    private BigDecimal markupPercentage;
    private BigDecimal weight;
    private String dimensions;

    private Integer stockQuantity;
    private Integer minStockLevel;

    private Boolean isActive;
    private Boolean featured;

    private Boolean isGold;
    private BigDecimal goldWeightGrams;
    private Integer goldPurityKarat;

    // Related DTO lists (define these DTOs separately)
    private List<ProductImageDto> images;
    private List<ProductCategoryDto> productCategories;
    private List<ProductAttributeValueDto> attributeValues;

    public Product orElseThrow(Object o) {
        return null;
    }
}
