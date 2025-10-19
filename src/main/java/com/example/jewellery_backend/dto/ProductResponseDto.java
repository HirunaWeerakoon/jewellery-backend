package com.example.jewellery_backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Product response DTO.
 * Used for returning detailed product information with its associated categories.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isActive;

    // List of categories the product belongs to
    private List<CategoryDto> categories;

    // Optional product details (include only if relevant in your entity)
    private String sku;       // Product SKU (identifier)
    private Integer stock;    // Available quantity
    private List<String> imageUrls; // Product image URLs (if applicable)
}
