package com.example.jewellery_backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDto {
    @NotBlank
    private String reviewerName;

    @NotBlank
    @Email
    private String reviewerEmail;

    @NotBlank
    private String commentText;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}