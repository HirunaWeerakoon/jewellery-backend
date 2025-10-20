package com.example.jewellery_backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartRequest {
    private Long productId;
    private Long attributeValueId; // optional

    @Builder.Default
    private Integer quantity = 1;
}
