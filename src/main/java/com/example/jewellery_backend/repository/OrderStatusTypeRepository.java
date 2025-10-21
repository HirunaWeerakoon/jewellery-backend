package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.OrderStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderStatusTypeRepository extends JpaRepository<OrderStatusType, Long> {
    // Method to find a status type by its enum name
    Optional<OrderStatusType> findByOrderStatusName(OrderStatusType.OrderStatus statusName);
}