package com.example.jewellery_backend.repository;
import com.example.jewellery_backend.entity.GoldRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoldRateRepository extends JpaRepository<GoldRate, Long> {
    Optional<GoldRate> findTopByOrderByDateDesc();
}
