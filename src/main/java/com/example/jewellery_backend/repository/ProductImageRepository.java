package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // find primary image if present
    Optional<ProductImage> findFirstByProductIdAndIsPrimaryTrue(Long productId);
    // fallback: get first by product sorted by sortOrder
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
}
