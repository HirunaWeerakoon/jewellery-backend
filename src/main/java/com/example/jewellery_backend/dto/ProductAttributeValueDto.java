package com.example.jewellery_backend.dto;

import lombok.*;

/**
 * DTO for transferring product attribute values.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttributeValueDto {

    private Integer id;

    // Reference IDs instead of entities
    private Long productId;
    private Long attributeValueId;

    // Optional: include attribute value name if needed
    private String attributeValueName;

    // Helper method to convert from entity to DTO
    public static ProductAttributeValueDto fromEntity(com.example.jewellery_backend.entity.ProductAttributeValue pav) {
        return ProductAttributeValueDto.builder()
                .id(pav.getId())
                .productId(pav.getProduct() != null ? pav.getProduct().getProductId() : null)
                .attributeValueId(pav.getAttributeValue() != null ? pav.getAttributeValue().getValueId() : null)
                .attributeValueName(pav.getAttributeValue() != null ? pav.getAttributeValue().getAttributeValue() : null)
                .build();
    }
}
