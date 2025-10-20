package com.example.jewellery_backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDto {
    private List<CartItemDto> items;
    private BigDecimal cartTotal;
    private Integer totalQuantity;
}
