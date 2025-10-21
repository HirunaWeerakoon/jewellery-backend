package com.example.jewellery_backend.controller;
import com.example.jewellery_backend.dto.OrderRequestDto;
import com.example.jewellery_backend.dto.OrderResponseDto;
import com.example.jewellery_backend.entity.Order;
import com.example.jewellery_backend.service.FileStorageService;
import com.example.jewellery_backend.service.OrderService;
import com.example.jewellery_backend.util.Mapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/orders") // Base path remains /orders
public class OrderController {

    private final OrderService orderService;
    // fileStorageService might not be directly needed here anymore, but OrderService uses it.
    private final FileStorageService fileStorageService; // Keep if needed by constructor injection

    // Constructor remains the same
    public OrderController(OrderService orderService, FileStorageService fileStorageService) {
        this.orderService = orderService;
        this.fileStorageService = fileStorageService;
    }

    // Combined endpoint for creating an order from session cart and uploading the slip
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<OrderResponseDto> createOrderAndUploadSlip(
            @RequestPart("orderData") @Valid OrderRequestDto orderRequestDto, // User details as JSON part
            @RequestPart(value = "slipFile", required = true) MultipartFile slipFile, // Slip file part
            HttpSession session // Inject HttpSession
    ) {
        // Calls the service method responsible for handling cart items, stock, slip, etc.
        Order createdOrder = orderService.createOrderFromSessionCart(orderRequestDto, slipFile, session);
        return ResponseEntity.status(HttpStatus.CREATED).body(Mapper.toOrderResponseDto(createdOrder));
    }
}