package com.example.jewellery_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long id;
    private Long productId;
    private String productName; // optional, can be null if not set
    private Double unitPrice;
    private Integer quantity;
    private Double subtotal;
}
