package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.ProductCategory;
import com.example.jewellery_backend.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.List;

@Transactional
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
    List<ProductCategory> findByIdCategoryId(Long categoryId);
    List<ProductCategory> findByIdProductId(Long productId);
    List<ProductCategory> findByIdCategoryIdIn(Set<Long> categoryIds);
}
