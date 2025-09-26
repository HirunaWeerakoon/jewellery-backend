package com.example.jewellery_backend;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public Map<Long, CartItem> getItems() { return items; }

    public void addItem(CartItem item) {
        items.merge(item.getProductId(), item, (oldV, newV) -> {
            oldV.setQuantity(oldV.getQuantity() + newV.getQuantity());
            return oldV;
        });
    }

    public void updateQty(Long productId, int qty) {
        if (qty <= 0) { items.remove(productId); return; }
        CartItem it = items.get(productId);
        if (it != null) it.setQuantity(qty);
    }

    public void remove(Long productId) { items.remove(productId); }

    public BigDecimal subtotal() {
        return items.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() { return items.isEmpty(); }
}
