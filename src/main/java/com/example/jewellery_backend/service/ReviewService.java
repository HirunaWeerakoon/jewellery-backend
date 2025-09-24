package com.example.jewellery_backend.service;

import com.example.jewellery_backend.entity.Review;
import com.example.jewellery_backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review addReview(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}
