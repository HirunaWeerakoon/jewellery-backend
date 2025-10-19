package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.*;

/**
 * Entity representing product reviews.
 */
@Entity
@Table(name = "reviews")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(name = "reviewer_name", nullable = false, length = 100)
    private String reviewerName;

    @Column(name = "reviewer_email", length = 100)
    private String reviewerEmail;

    @Column(name = "rating", nullable = false)
    private int rating; // 1 to 5

    @Column(name = "comment_text", columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "review_date", nullable = false)
    @Builder.Default
    private LocalDateTime reviewDate = LocalDateTime.now();

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = false;

    @PrePersist
    public void prePersist() {
        if (reviewDate == null) reviewDate = LocalDateTime.now();
        rating = Optional.ofNullable(rating).orElse(1);

    }

    // -------------------- Helper Methods --------------------
    public void approve() {
        this.isApproved = true;
    }

    public void disapprove() {
        this.isApproved = false;
    }
}
