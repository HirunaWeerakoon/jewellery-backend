package com.example.jewellery_backend;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
// Allow Next.js dev server (port 3000) to talk to this API and keep cookies
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class CartController {

    // --- get or create Cart in HttpSession ---
    private Cart getOrCreateCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("CART");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("CART", cart);
        }
        return cart;
    }

    // 1) Read cart
    @GetMapping
    public CartDto getCart(HttpSession session) {
        Cart cart = getOrCreateCart(session);
        return CartDto.from(cart, session.getId());
    }

    // 2) Add item
    @PostMapping("/items")
    public CartDto addItem(@RequestBody AddItemDto body, HttpSession session) {
        Cart cart = getOrCreateCart(session);
        if (body.quantity <= 0) body.quantity = 1; // safety default
        cart.addItem(new CartItem(body.productId, body.name, body.unitPrice, body.quantity));
        return CartDto.from(cart, session.getId());
    }

    // 3) Update quantity (remove if qty<=0)
    @PutMapping("/items/{productId}")
    public CartDto updateQty(@PathVariable Long productId, @RequestBody UpdateQtyDto body, HttpSession session) {
        Cart cart = getOrCreateCart(session);
        cart.updateQty(productId, body.quantity);
        return CartDto.from(cart, session.getId());
    }

    // 4) Remove item
    @DeleteMapping("/items/{productId}")
    public CartDto remove(@PathVariable Long productId, HttpSession session) {
        Cart cart = getOrCreateCart(session);
        cart.remove(productId);
        return CartDto.from(cart, session.getId());
    }

    // 5) Checkout → payload to hand off to payment step
    @PostMapping("/checkout")
    public CheckoutPayload checkout(HttpSession session) {
        Cart cart = getOrCreateCart(session);
        return CheckoutPayload.from(cart, session.getId());
    }

    // --- DTOs ---
    public static class AddItemDto {
        public Long productId;
        public String name;
        public BigDecimal unitPrice;
        public int quantity;
    }

    public static class UpdateQtyDto {
        public int quantity;
    }

    public record CartDto(String sessionId, List<Item> items, BigDecimal subtotal) {
        public record Item(Long productId, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
        public static CartDto from(Cart cart, String sid) {
            var list = cart.getItems().values().stream()
                    .map(i -> new Item(i.getProductId(), i.getName(), i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                    .toList();
            return new CartDto(sid, list, cart.subtotal());
        }
    }

    public record CheckoutPayload(String sessionId, List<CartDto.Item> items, BigDecimal subtotal) {
        public static CheckoutPayload from(Cart cart, String sid) {
            var list = cart.getItems().values().stream()
                    .map(i -> new CartDto.Item(i.getProductId(), i.getName(), i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                    .toList();
            return new CheckoutPayload(sid, list, cart.subtotal());
        }
    }
}

