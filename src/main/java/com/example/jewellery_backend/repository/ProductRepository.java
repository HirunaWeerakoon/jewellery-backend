package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"categories"}) // optional: if your Product entity has categories mapped
    List<Product> findAll();
}






