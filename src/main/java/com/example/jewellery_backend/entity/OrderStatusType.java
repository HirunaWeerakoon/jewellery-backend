package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing order status types.
 */
@Entity
@Table(name = "order_status_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_status_id")
    private Long orderStatusId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status_name", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus orderStatusName = OrderStatus.pending;

    public enum OrderStatus {
        pending,
        verified,
        paid,
        processing,
        shipped,
        delivered,
        cancelled,
        refunded
    }
}
