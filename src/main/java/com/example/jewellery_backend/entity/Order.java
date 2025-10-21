package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an order.
 */
@Entity
@Table(name = "orders")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "user_address", nullable = false, length = 255)
    private String userAddress;

    @Column(name = "telephone_number", nullable = false, length = 20)
    private String telephoneNumber;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_status_id", nullable = false)
    private OrderStatusType orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_status_id", nullable = false)
    private PaymentStatusType paymentStatus;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<Slip> slips = new ArrayList<>();

    // Lifecycle callbacks to handle timestamps
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------- Helper methods ----------------------
    public void addOrderItem(OrderItem item) {
        if (item != null) {
            orderItems.add(item);
            item.setOrder(this);
        }
    }

    public void removeOrderItem(OrderItem item) {
        if (item != null) {
            orderItems.remove(item);
            item.setOrder(null);
        }
    }

    public void setOrderItems(List<OrderItem> items) {
        for (OrderItem oi : new ArrayList<>(this.orderItems)) {
            removeOrderItem(oi);
        }
        if (items != null) {
            for (OrderItem i : items) {
                addOrderItem(i);
            }
        }
    }

    public void addSlip(Slip slip) {
        if (slip != null) {
            slips.add(slip);
            slip.setOrder(this);
        }
    }

    public void removeSlip(Slip slip) {
        if (slip != null) {
            slips.remove(slip);
            slip.setOrder(null);
        }
    }
}
