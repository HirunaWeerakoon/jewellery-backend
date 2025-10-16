package com.example.jewellery_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDto {
    // getters + setters
    @NotNull
    private String customerName;

    @NotNull
    @Email
    private String customerEmail;

    @NotNull
    private String customerAddress;

    @NotNull
    private String telephoneNumber;

    // totalAmount may be validated/calculated server-side too (we will calculate from items)
    private Double totalAmount;

    @NotNull
    private List<OrderItemRequestDto> items;

    // optional - if you add more payment methods later make an enum
    private String paymentMethod = "BANK_TRANSFER";
}
