package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
}
