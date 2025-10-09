package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Custom query method: find products by name containing a keyword (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String keyword);
}
