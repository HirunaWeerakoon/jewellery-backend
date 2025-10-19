package com.example.jewellery_backend.dto;

import lombok.*;

/**
 * DTO for transferring product image data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {

    private Long imageId;
    private String imageUrl;
    private String altText;
    private Boolean isPrimary;
    private Integer sortOrder;
}
