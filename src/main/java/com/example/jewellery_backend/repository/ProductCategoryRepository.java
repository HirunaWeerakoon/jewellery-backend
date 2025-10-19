package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.ProductCategory;
import com.example.jewellery_backend.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
    List<ProductCategory> findByCategoryId(Long categoryId);
    List<ProductCategory> findByProductId(Long productId);
}
