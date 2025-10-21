package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.CategoryClosure;
import com.example.jewellery_backend.entity.CategoryClosureId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CategoryClosureRepository extends JpaRepository<CategoryClosure, CategoryClosureId> {

    // Find all descendant category IDs for a given ancestor ID
    @Query("SELECT cc.descendant.categoryId FROM CategoryClosure cc WHERE cc.ancestor.categoryId = :ancestorId")
    Set<Long> findDescendantIdsByAncestorId(@Param("ancestorId") Long ancestorId);

    // Find direct children (depth = 1) - useful for tree structures
    List<CategoryClosure> findByAncestorCategoryIdAndDepth(Long ancestorId, Integer depth);

    // Find direct parent (depth = 1)
    CategoryClosure findByDescendantCategoryIdAndDepth(Long descendantId, Integer depth);
}