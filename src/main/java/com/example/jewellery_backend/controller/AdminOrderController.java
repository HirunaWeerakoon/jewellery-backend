package com.example.jewellery_backend.controller;

import com.example.jewellery_backend.dto.OrderResponseDto;
import com.example.jewellery_backend.dto.UpdateStatusDto;
import com.example.jewellery_backend.entity.Order;
import com.example.jewellery_backend.entity.OrderStatusType;
import com.example.jewellery_backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @RequestParam(value = "status", required = false) String statusStr) {

        List<OrderResponseDto> dtos;

        if (statusStr != null && !statusStr.isBlank()) {
            // Filter by order status
            OrderStatusType.OrderStatus statusEnum = OrderStatusType.OrderStatus.valueOf(statusStr);
            dtos = orderService.listAllOrders().stream() // Get DTO list from service
                    .filter(o -> o.getOrderStatusType() != null &&
                            o.getOrderStatusType().getOrderStatusName() == statusEnum)
                    .collect(Collectors.toList());
        } else {
            dtos = orderService.listAllOrders(); // Get DTO list directly
        }

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(Mapper.toOrderResponseDto(order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusDto dto) {

        Order updated = orderService.updateStatuses(
                id,
                dto.getOrderStatus(),
                dto.getPaymentStatus()
        );

        return ResponseEntity.ok(Mapper.toOrderResponseDto(updated));
    }
}
