
package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "orderItems") // avoids lazy loading issues
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name="customer_name", nullable = false)
    private String customerName;

    @Column(name="customer_email", nullable = false)
    private String customerEmail;

    @Column(name="total_amount", nullable = false)
    private Double totalAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false, length = 50)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Slip slip;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------- helper methods ----------------------

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
        // unlink existing
        for (OrderItem oi : new ArrayList<>(this.orderItems)) {
            removeOrderItem(oi);
        }
        if (items != null) {
            for (OrderItem i : items) {
                addOrderItem(i);
            }
        }
    }

    public void setSlip(Slip slip) {
        if (slip == null) {
            if (this.slip != null) {
                this.slip.setOrder(null);
            }
            this.slip = null;
        } else {
            slip.setOrder(this);
            this.slip = slip;
        }
    }
}
