package com.example.jewellery_backend.dto;

import com.example.jewellery_backend.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    public void setSubtotal(BigDecimal totalPrice) {
    }

    public void setUnitPrice(BigDecimal unitPrice) {
    }

    public void setProductId(Product product) {

    }
}
