package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.NonNull;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Load categories eagerly for all products
    @EntityGraph(attributePaths = {"productCategories", "productCategories.category"})
    @NonNull
    List<Product> findAll();

    //  Load categories eagerly but only for active products
    @EntityGraph(attributePaths = {"productCategories", "productCategories.category"})
    List<Product> findByIsActiveTrue();
}
