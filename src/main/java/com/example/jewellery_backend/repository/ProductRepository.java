package com.example.jewellery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.jewellery_backend.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // add custom queries here if needed (e.g., findByName)
}

