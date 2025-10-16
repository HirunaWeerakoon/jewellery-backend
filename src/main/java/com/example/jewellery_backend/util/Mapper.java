package com.example.jewellery_backend.util;

import com.example.jewellery_backend.dto.OrderItemResponseDto;
import com.example.jewellery_backend.dto.OrderResponseDto;
import com.example.jewellery_backend.entity.Order;
import com.example.jewellery_backend.entity.OrderItem;
import com.example.jewellery_backend.entity.Slip;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class Mapper {

    /**
     * Convert Order entity to OrderResponseDto
     */
    public static OrderResponseDto toOrderResponseDto(Order order) {
        OrderResponseDto orderResponseDto = new OrderResponseDto();

        // FIXED: getId() not getID()
        orderResponseDto.setId(order.getOrderId());
        orderResponseDto.setCustomerName(order.getUserName());
        orderResponseDto.setCustomerEmail(order.getUserEmail());
        orderResponseDto.setTotalAmount(order.getTotalAmount());
        orderResponseDto.setStatus(order.getOrderStatus());
        orderResponseDto.setCreatedAt(order.getCreatedAt());

        // Convert OrderItems to OrderItemResponseDto
        List<OrderItemResponseDto> items = order.getOrderItems() == null ? List.of() :
                order.getOrderItems().stream()
                        .map(Mapper::toItemResponse)
                        .collect(Collectors.toList());

        // Set items in DTO
        orderResponseDto.setItems(items);

// Set slip info if exists (use first slip)
        if (order.getSlips() != null && !order.getSlips().isEmpty()) {
            Slip slip = order.getSlips().get(0);
            orderResponseDto.setSlipFileName(slip.getFileName());
            orderResponseDto.setSlipFilePath(slip.getFilePath());
        }


        return orderResponseDto;
    }

    /**
     * Convert OrderItem entity to OrderItemResponseDto
     */
    public static OrderItemResponseDto toItemResponse(OrderItem item) {
        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setId(item.getOrderItemId());
        dto.setProductId(item.getProduct());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getTotalPrice());

        // Optional: productName can be set later if needed
        return dto;
    }
}
