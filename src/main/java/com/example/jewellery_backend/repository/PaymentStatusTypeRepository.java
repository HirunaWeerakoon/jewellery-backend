package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.PaymentStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentStatusTypeRepository extends JpaRepository<PaymentStatusType, Long> {
    // Method to find a status type by its enum name
    Optional<PaymentStatusType> findByPaymentStatusName(PaymentStatusType.PaymentStatus statusName);
}