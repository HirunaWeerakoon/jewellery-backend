package com.example.jewellery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.jewellery_backend.entity.Slip;

import java.util.Optional;


public interface SlipRepository extends JpaRepository<Slip, Long> {
    Optional<Slip> findByOrderId(Long orderId);
    void deleteByOrderId(Long orderId);
}
