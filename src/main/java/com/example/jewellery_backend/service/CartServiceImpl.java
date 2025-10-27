package com.example.jewellery_backend.service;

import com.example.jewellery_backend.Cart;
import com.example.jewellery_backend.CartItem;
import com.example.jewellery_backend.dto.*;
import com.example.jewellery_backend.exception.ProductNotFoundException;
import com.example.jewellery_backend.util.CartMapper;
import com.example.jewellery_backend.repository.*;
import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.entity.ProductImage;
import com.example.jewellery_backend.service.ProductService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductService productService;

    private Cart getOrCreateCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(Cart.SESSION_ATTRIBUTE);
        if (cart == null) {
            cart = Cart.builder().build();
            session.setAttribute(Cart.SESSION_ATTRIBUTE, cart);
        }
        return cart;
    }

    @Override
    public CartResponseDto getCart(HttpSession session) {
        Cart cart = getOrCreateCart(session);
        return toDto(cart);
    }

    @Override
    public CartResponseDto addToCart(HttpSession session, AddToCartRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(req.getProductId()));

        BigDecimal price = productService.getUpdatedPrice(product.getProductId());

        String itemKey = CartMapper.makeItemKey(product.getProductId(), req.getAttributeValueId());
        CartItem item = CartItem.builder()
                .itemKey(itemKey)
                .productId(product.getProductId())
                .attributeValueId(req.getAttributeValueId())
                .productName(getProductName(product))
                .quantity(req.getQuantity() == null ? 1 : req.getQuantity())
                .unitPrice(price)
                .build();
        item.recalcTotal();

        // attach image
        attachPrimaryImage(product.getProductId(), item);

        Cart cart = getOrCreateCart(session);
        cart.addItem(item);
        session.setAttribute(Cart.SESSION_ATTRIBUTE, cart);

        return toDto(cart);
    }


    @Override
    public CartResponseDto updateCartItem(HttpSession session, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(session);
        CartItem item = cart.getItems().get(req.getItemKey());
        if (item != null) {
            BigDecimal updatedPrice = productService.getUpdatedPrice(item.getProductId());
            item.setUnitPrice(updatedPrice); // Update unit price before quantity
            cart.updateQuantity(req.getItemKey(), req.getQuantity()); // Recalcs total
        } else {

            System.err.println("Warning: Cart item not found for key: " + req.getItemKey());
        }

        session.setAttribute(Cart.SESSION_ATTRIBUTE, cart);
        return toDto(cart);
    }

    @Override
    public CartResponseDto removeItem(HttpSession session, String itemKey) {
        Cart cart = getOrCreateCart(session);
        cart.removeItem(itemKey);
        session.setAttribute(Cart.SESSION_ATTRIBUTE, cart);
        return toDto(cart);
    }

    @Override
    public CartResponseDto clearCart(HttpSession session) {
        Cart cart = getOrCreateCart(session);
        cart.clear();
        session.setAttribute(Cart.SESSION_ATTRIBUTE, cart);
        return toDto(cart);
    }

    private BigDecimal determinePrice(Product product, Long attributeValueId) {
        // Use product.getPrice() by default. If attributes change price, retrieve attribute and compute.
        // Replace getPrice() with actual getter if different.
        try {
            return product.getBasePrice();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void attachPrimaryImage(Long productId, CartItem item) {
        // Try to find a primary image; fallback to first ordered image
        Optional<ProductImage> primary = Optional.empty();
        try {
            // Try repository method by productId and isPrimary
            primary = productImageRepository.findFirstByProduct_ProductIdAndIsPrimaryTrue(productId);
        } catch (Exception ignore) {
            // Some projects need findFirstByProduct_IdAndIsPrimaryTrue depending on naming
            try {
                primary = Optional.empty();
            } catch (Exception e) { /* ignore */ }
        }

        if (primary.isEmpty()) {
            // fallback to first by sort order
            var list = productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(productId);
            if (!list.isEmpty()) primary = Optional.of(list.get(0));
        }

        primary.ifPresent(img -> {
            item.setImageUrl(img.getImageUrl());
            item.setImageAlt(img.getAltText());
        });
    }

    private String getProductName(Product product) {
        // replace getName() if your entity uses a different getter
        try {
            return product.getProductName();
        } catch (Exception e) {
            return "Product-" + product.getProductId();
        }
    }

    private CartResponseDto toDto(Cart cart) {
        return CartResponseDto.builder()
                .items(cart.getItemList().stream().map(CartMapper::toDto).collect(Collectors.toList()))
                .cartTotal(cart.getCartTotal())
                .totalQuantity(cart.getTotalQuantity())
                .build();
    }
}
