package com.example.jewellery_backend.service.impl;


import com.example.jewellery_backend.dto.ReviewRequestDto;
import com.example.jewellery_backend.dto.ReviewResponseDto;
import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.entity.Review;
import com.example.jewellery_backend.repository.ProductRepository;
import com.example.jewellery_backend.repository.ReviewRepository;
import com.example.jewellery_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {


    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;


    @Override
    public ReviewResponseDto addReview(Long productId, ReviewRequestDto request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));


        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .product(product)
                .reviewerName(request.getReviewerName())
                .reviewerEmail(request.getReviewerEmail())
                .commentText(request.getCommentText())
                .rating(request.getRating())
                .isApproved(false) // default: admin approval required; change if you want auto-approve
                .build();


        Review saved = reviewRepository.save(review);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getPublicReviewsForProduct(Long productId) {
        return reviewRepository.findByProduct_ProductIdAndIsApprovedTrueOrderByReviewDateDesc(productId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getAllReviewsForProduct(Long productId) {
        return reviewRepository.findByProduct_ProductIdOrderByReviewDateDesc(productId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getAllReviews() {
        return reviewRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new IllegalArgumentException("Review not found: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }


    @Override
    public void approveReview(Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));
        r.setIsApproved(true);
        reviewRepository.save(r);
    }
    private ReviewResponseDto mapToDto(Review r) {
        return ReviewResponseDto.builder()
                .reviewId(r.getReviewId())
                .reviewerName(r.getReviewerName())
                .reviewerEmail(r.getReviewerEmail())
                .commentText(r.getCommentText())
                .rating(r.getRating())
                .reviewDate(r.getReviewDate())
                .isApproved(r.getIsApproved())
                .build();
    }
}

