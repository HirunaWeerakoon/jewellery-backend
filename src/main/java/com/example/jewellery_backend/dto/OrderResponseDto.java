package com.example.jewellery_backend.dto;

import com.example.jewellery_backend.entity.OrderStatusType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long id;
    private String customerName;
    private String customerAddress;
    private String customerEmail;
    private BigDecimal totalAmount;
    private OrderStatusType orderStatusType;
    private LocalDateTime createdAt;

    private List<OrderItemResponseDto> items; // must have getter/setter for Mapper

    private String slipFileName; // optional
    private String slipFilePath; // optional
}
