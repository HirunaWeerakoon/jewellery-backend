package com.example.jewellery_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    // getters + setters
    @NotBlank(message = "Name is required")
    private String customerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String customerEmail;

    @NotBlank(message = "Address is required")
    @Size(max = 512)
    private String customerAddress;

    @NotBlank(message = "Telephone Number is required")
    // simple telephone regex; adjust to your locale if needed
    @Pattern(regexp = "^[0-9+\\-() ]{6,25}$", message = "Telephone looks invalid")
    private String telephoneNumber;

    // totalAmount may be validated/calculated server-side too (we will calculate from items)
    private Double totalAmount;

    private List<OrderItemRequestDto> items;
}
