package com.example.jewellery_backend;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    public static final String SESSION_ATTRIBUTE = "CART";

    @Builder.Default
    private Map<String, CartItem> items = new LinkedHashMap<>();

    public void addItem(CartItem item) {
        CartItem existing = items.get(item.getItemKey());
        if (existing == null) {
            item.recalcTotal();
            items.put(item.getItemKey(), item);
        } else {
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
            existing.recalcTotal();
        }
    }

    public void updateQuantity(String itemKey, int quantity) {
        CartItem it = items.get(itemKey);
        if (it != null) {
            it.setQuantity(quantity);
            it.recalcTotal();
            if (it.getQuantity() <= 0) items.remove(itemKey);
        }
    }

    public void removeItem(String itemKey) {
        items.remove(itemKey);
    }

    public List<CartItem> getItemList() {
        return new ArrayList<>(items.values());
    }

    public BigDecimal getCartTotal() {
        return items.values().stream()
                .map(CartItem::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalQuantity() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void clear() {
        items.clear();
    }
}
