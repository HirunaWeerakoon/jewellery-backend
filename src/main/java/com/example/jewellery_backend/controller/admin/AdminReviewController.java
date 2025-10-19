package com.example.jewellery_backend.controller.admin;


import com.example.jewellery_backend.dto.ReviewResponseDto;
import com.example.jewellery_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {


    private final ReviewService reviewService;


    // list all reviews (admin)
    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> listAll() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }


    // list all reviews for a given product (admin)
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDto>> listByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getAllReviewsForProduct(productId));
    }


    // approve a review (admin)
    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long reviewId) {
        reviewService.approveReview(reviewId);
        return ResponseEntity.noContent().build();
    }


    // delete a review (admin)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}