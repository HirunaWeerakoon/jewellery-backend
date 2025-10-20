package com.example.jewellery_backend.repository;
import com.example.jewellery_backend.entity.GoldRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface GoldRateRepository extends JpaRepository<GoldRate, Long> {
    Optional<GoldRate> findTopByOrderByEffectiveDateDesc();

}
