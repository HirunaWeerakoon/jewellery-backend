package com.example.jewellery_backend.util;

import com.example.jewellery_backend.CartItem;
import com.example.jewellery_backend.dto.CartItemDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CartMapper {

    public static String makeItemKey(Long productId, Long attributeValueId) {
        return productId + ":" + (attributeValueId == null ? "0" : attributeValueId.toString());
    }


    public static CartItemDto toDto(CartItem item) {
        return CartItemDto.builder()
                .itemKey(item.getItemKey())
                .productId(item.getProductId())
                .attributeValueId(item.getAttributeValueId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .imageUrl(item.getImageUrl())
                .imageAlt(item.getImageAlt())
                .build();
    }
}
