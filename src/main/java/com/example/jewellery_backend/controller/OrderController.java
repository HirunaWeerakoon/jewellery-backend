package com.example.jewellery_backend.controller;

import com.example.jewellery_backend.dto.OrderRequestDto;
import com.example.jewellery_backend.dto.OrderResponseDto;
import com.example.jewellery_backend.entity.Order;
import com.example.jewellery_backend.entity.OrderStatus;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for customer checkout (orders) and slip upload endpoints used by both customer and admin flows.
 *
 * Key endpoints:
 *  - POST /orders                      : place order (with items)
 *  - GET  /orders                      : list orders (optional ?status=...)
 *  - GET  /orders/{id}                 : get order details
 *  - PUT  /orders/{id}/status          : update order status (admin)
 *
 *  - POST /orders/{id}/slip            : upload slip (customer) -> sets OrderStatus.SLIP_UPLOADED
 *  - PUT  /orders/{id}/slip            : replace slip
 *  - GET  /orders/{id}/slip            : download/stream slip
 *  - DELETE /orders/{id}/slip          : delete slip
 */
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

    /**
     * Place a new order (checkout). Request must include items inside OrderRequestDto.
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        Order created = orderService.createOrder(orderRequestDto);
        OrderResponseDto resp = Mapper.toOrderResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * List orders. Optionally filter by status using ?status=SLIP_UPLOADED or ?status=PENDING etc.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> listOrders(@RequestParam(required = false) OrderStatus status) {
        List<Order> orders;
        if (status != null) orders = orderService.findByStatus(status);
        else orders = orderService.findAll();

        List<OrderResponseDto> resp = orders.stream()
                .map(Mapper::toOrderResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    /**
     * Get order details by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(Mapper.toOrderResponseDto(order));
    }

    /**
     * Update order status (admin use). Accepts body containing new status like {"status":"VERIFIED"}.
     * You can adapt to a specific DTO if you prefer.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long id,
                                                              @RequestParam("status") OrderStatus status) {
        Order updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(Mapper.toOrderResponseDto(updated));
    }

    // -------------------- Slip endpoints --------------------

    /**
     * Upload a slip file for an order. This will set order status to OrderStatus.SLIP_UPLOADED
     * (ensuring we always use the enum constant SLIP_UPLOADED).
     *
     * curl example:
     * curl -F "file=@receipt.jpg" http://localhost:8080/orders/1/slip
     */
    @PostMapping("/{id}/slip")
    public ResponseEntity<?> uploadSlip(@PathVariable("id") Long id,
                                        @RequestPart("file") MultipartFile file) {
        // orderService.uploadSlip(...) will store the file, persist Slip, and set OrderStatus.SLIP_UPLOADED
        Slip savedSlip = orderService.uploadSlip(id, file);

        // return slip metadata in response for client convenience
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new Object() {
                    public final Long slipId = savedSlip.getId();
                    public final String fileName = savedSlip.getFileName();
                    public final String filePath = savedSlip.getFilePath();
                    public final String fileType = savedSlip.getFileType();
                    public final Long fileSize = savedSlip.getFileSize();
                }
        );
    }

    /**
     * Replace an existing slip (alias to upload, will replace previous file and keep status SLIP_UPLOADED).
     */
    @PutMapping("/{id}/slip")
    public ResponseEntity<?> replaceSlip(@PathVariable("id") Long id,
                                         @RequestPart("file") MultipartFile file) {
        Slip slip = orderService.replaceSlip(id, file);
        return ResponseEntity.ok(
                new Object() {
                    public final Long slipId = slip.getId();
                    public final String fileName = slip.getFileName();
                    public final String filePath = slip.getFilePath();
                    public final String fileType = slip.getFileType();
                    public final Long fileSize = slip.getFileSize();
                }
        );
    }

    /**
     * Stream / download the slip file associated with the order.
     * This uses FileStorageService.loadAsResource(...) under the hood for safe file reading.
     */
    @GetMapping("/{id}/slip")
    public ResponseEntity<Resource> getSlipFile(@PathVariable("id") Long id) {
        Slip slip = orderService.getSlip(id);
        Resource resource = fileStorageService.loadAsResource(slip.getFilePath());

        String contentType = slip.getFileType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : slip.getFileType();
        HttpHeaders headers = new HttpHeaders();
        ContentDisposition cd = ContentDisposition.builder("inline")
                .filename(slip.getFileName())
                .build();
        headers.setContentDisposition(cd);
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(slip.getFileSize());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Delete slip for an order (customer/admin). This will remove the file and DB record and set order back to PENDING.
     */
    @DeleteMapping("/{id}/slip")
    public ResponseEntity<Void> deleteSlip(@PathVariable("id") Long id) {
        orderService.deleteSlip(id);
        return ResponseEntity.noContent().build();
    }
}
