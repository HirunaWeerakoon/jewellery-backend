package com.example.jewellery_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequestDto {
    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;

    // optional: unitPrice (if client sends price); otherwise service will fetch product price
    private Double unitPrice;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
}
