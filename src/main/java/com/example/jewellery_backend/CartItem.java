package com.example.jewellery_backend;

import java.math.BigDecimal;

public class CartItem {
    private Long productId;
    private String name;
    private BigDecimal unitPrice;
    private int quantity;

    public CartItem() {}

    public CartItem(Long productId, String name, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    public void setProductId(Long productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
