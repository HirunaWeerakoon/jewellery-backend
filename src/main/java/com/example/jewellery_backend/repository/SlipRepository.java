package com.example.jewellery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.jewellery_backend.entity.Slip;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlipRepository extends JpaRepository<Slip, Long> {

    void deleteByOrder_OrderId(Long orderId);

    Optional<Slip> findByOrder_OrderId(Long orderId);

}
