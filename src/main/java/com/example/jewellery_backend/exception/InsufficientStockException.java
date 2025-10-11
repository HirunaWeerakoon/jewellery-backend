package com.example.jewellery_backend.exception;

/**
 * Thrown when requested quantity exceeds product stock.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException() { super(); }
    public InsufficientStockException(String message) { super(message); }
    public InsufficientStockException(String message, Throwable cause) { super(message, cause); }
}
