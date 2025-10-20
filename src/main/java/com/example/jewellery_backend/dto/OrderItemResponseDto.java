package com.example.jewellery_backend.dto;

// Remove the unused import for Product
// import com.example.jewellery_backend.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal; // Import BigDecimal

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long id;
    private Long productId; // Use Long for ID
    private String productName;
    private BigDecimal unitPrice; // Changed from Double
    private Integer quantity;
    private BigDecimal subtotal; // Changed from Double

    // Custom setters REMOVED. Lombok will generate correct ones.
}