package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.ReviewRequestDto;
import com.example.jewellery_backend.dto.ReviewResponseDto;

import java.util.List;

public interface ReviewService {
    ReviewResponseDto addReview(Long productId, ReviewRequestDto request);
    List<ReviewResponseDto> getPublicReviewsForProduct(Long productId); // only approved
    List<ReviewResponseDto> getAllReviewsForProduct(Long productId); // admin: all
    List<ReviewResponseDto> getAllReviews(); // admin
    void deleteReview(Long reviewId);
    void approveReview(Long reviewId);
}
