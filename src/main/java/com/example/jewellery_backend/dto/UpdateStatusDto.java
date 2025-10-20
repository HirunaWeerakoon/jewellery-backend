package com.example.jewellery_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusDto {
    @NotBlank
    private String orderStatus;

    @NotBlank
    private String paymentStatus;
}
