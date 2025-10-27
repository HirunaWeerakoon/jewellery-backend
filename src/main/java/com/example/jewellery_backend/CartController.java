package com.example.jewellery_backend;

import com.example.jewellery_backend.dto.*;
import com.example.jewellery_backend.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> viewCart(HttpSession session) {
        return ResponseEntity.ok(cartService.getCart(session));
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponseDto> addToCart(HttpSession session, @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(cartService.addToCart(session, req));
    }

    @PutMapping("/item")
    public ResponseEntity<CartResponseDto> updateItem(HttpSession session, @RequestBody UpdateCartItemRequest req) {
        return ResponseEntity.ok(cartService.updateCartItem(session, req));
    }

    @DeleteMapping("/item/{itemKey}")
    public ResponseEntity<CartResponseDto> removeItem(HttpSession session, @PathVariable String itemKey) {
        return ResponseEntity.ok(cartService.removeItem(session, itemKey));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartResponseDto> clearCart(HttpSession session) {
        return ResponseEntity.ok(cartService.clearCart(session));
    }
}


