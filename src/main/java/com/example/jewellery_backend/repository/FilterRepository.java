package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    // No extra methods required; specifications will be used for filtering
}
