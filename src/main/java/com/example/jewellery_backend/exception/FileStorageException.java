package com.example.jewellery_backend.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String msg) { super(msg); }
    public FileStorageException(String msg, Throwable t) { super(msg, t); }
}
