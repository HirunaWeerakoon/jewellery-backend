package com.example.jewellery_backend;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private String itemKey; // e.g. "productId:attributeValueId"
    private Long productId;
    private Long attributeValueId; // nullable
    private String productName;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Integer quantity;

    // product image fields
    private String imageUrl;
    private String imageAlt;

    public void recalcTotal() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
}
