package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository already gives you:
    // findAll(), save(), findById(), deleteById()
}
