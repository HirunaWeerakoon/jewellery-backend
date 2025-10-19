package com.example.jewellery_backend.exception;

public class CartException extends RuntimeException {
    public CartException(String msg) { super(msg); }
    public CartException(String msg, Throwable t) { super(msg, t); }
}
