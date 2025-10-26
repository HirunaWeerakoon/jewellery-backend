package gemshop.service;

import gemshop.domain.*;
import gemshop.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final GemstoneRepository gemRepo;

    public CartService(CartRepository cartRepo, CartItemRepository itemRepo, GemstoneRepository gemRepo) {
        this.cartRepo = cartRepo;
        this.itemRepo = itemRepo;
        this.gemRepo = gemRepo;
    }

    @Transactional
    public Cart getOrCreateOpenCart(Long userId) {
        return cartRepo.findByUserIdAndStatus(userId, "OPEN")
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    c.setStatus("OPEN");
                    return cartRepo.save(c);
                });
    }

    @Transactional
    public Cart addItem(Long userId, Long gemstoneId, int qty) {
        if (qty <= 0) throw new RuntimeException("qty must be > 0");

        Cart cart = getOrCreateOpenCart(userId);

        // if item exists, just bump qty
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(ci -> ci.getGemstone().getId().equals(gemstoneId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + qty);
            return cartRepo.save(cart);
        } else {
            Gemstone gem = gemRepo.findById(gemstoneId)
                    .orElseThrow(() -> new RuntimeException("gem not found"));

            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setGemstone(gem);
            ci.setQuantity(qty);
            ci.setUnitPrice(gem.getBasePrice()); // snapshot

            cart.getItems().add(ci);
            return cartRepo.save(cart);
        }
    }

    @Transactional
    public Cart updateItem(Long userId, Long itemId, int qty) {
        if (qty <= 0) throw new RuntimeException("qty must be > 0");
        CartItem item = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("item not found"));
        // naive user check (no auth layer yet)
        if (!item.getCart().getUserId().equals(userId)) throw new RuntimeException("not your cart");
        item.setQuantity(qty);
        itemRepo.save(item);
        return getOrCreateOpenCart(userId);
    }

    @Transactional
    public Cart removeItem(Long userId, Long itemId) {
        CartItem item = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("item not found"));
        if (!item.getCart().getUserId().equals(userId)) throw new RuntimeException("not your cart");
        itemRepo.delete(item);
        return getOrCreateOpenCart(userId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> viewCart(Long userId) {
        Cart cart = getOrCreateOpenCart(userId);
        BigDecimal subtotal = cart.getItems().stream()
                .map(ci -> ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("cartId", cart.getId());
        res.put("userId", cart.getUserId());
        res.put("status", cart.getStatus());
        res.put("items", cart.getItems().stream().map(ci -> Map.of(
                "itemId", ci.getId(),
                "gemstoneId", ci.getGemstone().getId(),
                "name", ci.getGemstone().getName(),
                "qty", ci.getQuantity(),
                "unitPrice", ci.getUnitPrice(),
                "lineTotal", ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()))
        )).toList());
        res.put("subtotal", subtotal);
        // no taxes/discounts now
        res.put("total", subtotal);
        return res;
    }

    @Transactional
    public Map<String, Object> checkout(Long userId) {
        Cart cart = cartRepo.findByUserIdAndStatus(userId, "OPEN")
                .orElseThrow(() -> new RuntimeException("no open cart"));
        Map<String, Object> summary = viewCart(userId);
        cart.setStatus("CHECKED_OUT");
        cartRepo.save(cart);
        return summary;
    }
}
