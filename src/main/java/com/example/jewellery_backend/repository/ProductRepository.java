package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    //  add custom query methods
}
