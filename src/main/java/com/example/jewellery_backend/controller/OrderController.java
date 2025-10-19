package com.example.jewellery_backend.controller;

import com.example.jewellery_backend.dto.OrderRequestDto;
import com.example.jewellery_backend.dto.OrderResponseDto;
import com.example.jewellery_backend.dto.UpdateStatusDto;
import com.example.jewellery_backend.entity.Order;
import com.example.jewellery_backend.entity.OrderStatusType;
import com.example.jewellery_backend.entity.Slip;
import com.example.jewellery_backend.service.FileStorageService;
import com.example.jewellery_backend.service.OrderService;
import com.example.jewellery_backend.util.Mapper;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final FileStorageService fileStorageService;

    public OrderController(OrderService orderService, FileStorageService fileStorageService) {
        this.orderService = orderService;
        this.fileStorageService = fileStorageService;
    }

    // -------------------- Order endpoints --------------------

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        Order created = orderService.createOrder(orderRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Mapper.toOrderResponseDto(created));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @RequestParam(value = "status", required = false) String statusStr) {

        List<Order> orders;

        if (statusStr != null && !statusStr.isBlank()) {
            // Filter by status
            OrderStatusType.OrderStatus statusEnum = OrderStatusType.OrderStatus.valueOf(statusStr);
            orders = orderService.listAllOrders().stream()
                    .filter(o -> o.getOrderStatus() != null &&
                            o.getOrderStatus().getOrderStatusName() == statusEnum)
                    .collect(Collectors.toList());
        } else {
            orders = orderService.listAllOrders();
        }

        List<OrderResponseDto> resp = orders.stream()
                .map(Mapper::toOrderResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(Mapper.toOrderResponseDto(order));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusDto statusDto) {

        Order updated = orderService.updateStatuses(
                id,
                statusDto.getOrderStatus(),
                statusDto.getPaymentStatus()
        );

        return ResponseEntity.ok(Mapper.toOrderResponseDto(updated));
    }

    // -------------------- Slip endpoints --------------------

    @PostMapping("/{id}/slip")
    public ResponseEntity<?> uploadSlip(@PathVariable("id") Long id,
                                        @RequestPart("file") MultipartFile file) {

        Slip savedSlip = orderService.uploadSlip(id, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new Object() {
                    public final Long slipId = savedSlip.getSlipId();
                    public final String fileName = savedSlip.getFileName();
                    public final String filePath = savedSlip.getFilePath();
                    public final String fileType = savedSlip.getFileType();
                    public final Long fileSize = savedSlip.getFileSize();
                }
        );
    }

    @PutMapping("/{id}/slip")
    public ResponseEntity<?> replaceSlip(@PathVariable("id") Long id,
                                         @RequestPart("file") MultipartFile file) {

        Slip slip = orderService.replaceSlip(id, file);

        return ResponseEntity.ok(
                new Object() {
                    public final Long slipId = slip.getSlipId();
                    public final String fileName = slip.getFileName();
                    public final String filePath = slip.getFilePath();
                    public final String fileType = slip.getFileType();
                    public final Long fileSize = slip.getFileSize();
                }
        );
    }

    @GetMapping("/{id}/slip")
    public ResponseEntity<Resource> getSlipFile(@PathVariable("id") Long id) {

        Slip slip = orderService.getSlip(id);
        Resource resource = fileStorageService.loadAsResource(slip.getFilePath());

        String contentType = (slip.getFileType() != null)
                ? slip.getFileType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        HttpHeaders headers = new HttpHeaders();
        ContentDisposition cd = ContentDisposition.builder("inline")
                .filename(slip.getFileName())
                .build();

        headers.setContentDisposition(cd);
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(slip.getFileSize());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/slip")
    public ResponseEntity<Void> deleteSlip(@PathVariable("id") Long id) {
        orderService.deleteSlip(id);
        return ResponseEntity.noContent().build();
    }
}
