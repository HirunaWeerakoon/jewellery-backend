package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.*;
import jakarta.servlet.http.HttpSession;

public interface CartService {
    CartResponseDto getCart(HttpSession session);
    CartResponseDto addToCart(HttpSession session, AddToCartRequest req);
    CartResponseDto updateCartItem(HttpSession session, UpdateCartItemRequest req);
    CartResponseDto removeItem(HttpSession session, String itemKey);
    CartResponseDto clearCart(HttpSession session);
}
