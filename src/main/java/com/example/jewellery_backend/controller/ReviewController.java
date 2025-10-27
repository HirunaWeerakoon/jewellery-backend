package com.example.jewellery_backend.controller;


import com.example.jewellery_backend.dto.ReviewRequestDto;
import com.example.jewellery_backend.dto.ReviewResponseDto;
import com.example.jewellery_backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/products/{productId}/reviews")
public class ReviewController {


    private final ReviewService reviewService;


    @PostMapping
    public ResponseEntity<ReviewResponseDto> addReview(@PathVariable Long productId,
                                                       @Valid @RequestBody ReviewRequestDto request) {
        ReviewResponseDto saved = reviewService.addReview(productId, request);
        return ResponseEntity.ok(saved);
    }


    // Public: only approved reviews
    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getApprovedReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getPublicReviewsForProduct(productId));
    }
}