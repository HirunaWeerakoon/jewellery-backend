package com.example.jewellery_backend.exception;

public class ProductNotFoundException extends CartException {
    public ProductNotFoundException(Long productId) {
        super("Product not found: " + productId);    }
}
