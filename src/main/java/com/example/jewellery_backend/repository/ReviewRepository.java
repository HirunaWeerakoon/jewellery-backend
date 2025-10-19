package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get all reviews for a product
    List<Review> findByProduct_ProductId(Long productId);

    // Get approved reviews for a product, ordered by reviewDate descending
    List<Review> findByProduct_ProductIdAndIsApprovedTrueOrderByReviewDateDesc(Long productId);

    // Get all reviews for a product, ordered by reviewDate descending
    List<Review> findByProduct_ProductIdOrderByReviewDateDesc(Long productId);
}

