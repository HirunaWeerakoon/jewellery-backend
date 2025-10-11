package com.example.jewellery_backend.entity;

public enum OrderStatus {
    PENDING,          // order placed, waiting for payment or slip
    SLIP_UPLOADED,    // customer uploaded payment slip
    PAID,             // payment confirmed (optional)
    VERIFIED,         // admin verified the slip/payment
    COMPLETED,        // shipped / completed
    CANCELLED         // cancelled by customer or admin
}

