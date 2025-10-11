package com.example.jewellery_backend.util;

import com.example.jewellery_backend.dto.OrderItemResponseDto;
import com.example.jewellery_backend.dto.OrderResponseDto;
import com.example.jewellery_backend.entity.Order;
import com.example.jewellery_backend.entity.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class Mapper {

    /**
     * Convert Order entity to OrderResponseDto
     */
    public static OrderResponseDto toOrderResponseDto(Order order) {
        OrderResponseDto orderResponseDto = new OrderResponseDto();

        // FIXED: getId() not getID()
        orderResponseDto.setId(order.getId());
        orderResponseDto.setCustomerName(order.getCustomerName());
        orderResponseDto.setCustomerEmail(order.getCustomerEmail());
        orderResponseDto.setTotalAmount(order.getTotalAmount());
        orderResponseDto.setStatus(order.getStatus());
        orderResponseDto.setCreatedAt(order.getCreatedAt());

        // Convert OrderItems to OrderItemResponseDto
        List<OrderItemResponseDto> items = order.getOrderItems() == null ? List.of() :
                order.getOrderItems().stream()
                        .map(Mapper::toItemResponse)
                        .collect(Collectors.toList());

        // Set items in DTO
        orderResponseDto.setItems(items);

        // Set slip info if exists
        if (order.getSlip() != null) {
            orderResponseDto.setSlipFileName(order.getSlip().getFileName());
            orderResponseDto.setSlipFilePath(order.getSlip().getFilePath());
        }

        return orderResponseDto;
    }

    /**
     * Convert OrderItem entity to OrderItemResponseDto
     */
    public static OrderItemResponseDto toItemResponse(OrderItem item) {
        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());

        // Optional: productName can be set later if needed
        return dto;
    }
}
