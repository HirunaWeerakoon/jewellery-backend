package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing payment status types.
 */
@Entity
@Table(name = "payment_status_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_status_id")
    private Long paymentStatusId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status_name", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatusName = PaymentStatus.pending;

    public enum PaymentStatus {
        pending,
        failed,
        verified,
        refunded
    }
}
