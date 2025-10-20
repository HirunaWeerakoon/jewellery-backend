package com.example.jewellery_backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private String reviewerName;
    private String reviewerEmail;
    private String commentText;
    private Integer rating;
    private LocalDateTime reviewDate;
    private Boolean isApproved;
}