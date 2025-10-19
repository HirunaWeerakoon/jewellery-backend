package com.example.jewellery_backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryDto {

    private Long productId;
    private String productName; // optional, for easier display

    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private Boolean categoryIsActive;
}
