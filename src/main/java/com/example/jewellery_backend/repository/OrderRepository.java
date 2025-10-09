package com.example.jewellery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.jewellery_backend.entity.Order;
import com.example.jewellery_backend.entity.OrderStatus;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    // Optional: use Pageable if you want paging:
    // Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
