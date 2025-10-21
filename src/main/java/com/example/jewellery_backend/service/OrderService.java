package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.OrderItemRequestDto;
import com.example.jewellery_backend.dto.OrderRequestDto;
import com.example.jewellery_backend.entity.*;
import com.example.jewellery_backend.exception.InsufficientStockException;
import com.example.jewellery_backend.exception.ResourceNotFoundException;
import com.example.jewellery_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final SlipRepository slipRepository;
    private final FileStorageService fileStorageService;
    private final OrderStatusTypeRepository orderStatusTypeRepository;
    private final PaymentStatusTypeRepository paymentStatusTypeRepository;

    public OrderService(OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                ProductRepository productRepository,
                                SlipRepository slipRepository,
                                FileStorageService fileStorageService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.slipRepository = slipRepository;
        this.fileStorageService = fileStorageService;
    }

    // ---------------- Create Order (Admin or Checkout) ----------------

    @Transactional
    public Order createOrder(OrderRequestDto orderRequestDto) {
        if (orderRequestDto == null || orderRequestDto.getItems() == null || orderRequestDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order request must contain items");
        }

        Order order = new Order();
        order.setUserName(orderRequestDto.getCustomerName());
        order.setUserEmail(orderRequestDto.getCustomerEmail());
        order.setUserAddress(orderRequestDto.getCustomerAddress());
        order.setTelephoneNumber(orderRequestDto.getTelephoneNumber());
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDto itemReq : orderRequestDto.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            int qty = itemReq.getQuantity();
            if (qty <= 0) throw new IllegalArgumentException("Quantity must be >=1");

            if (product.getStockQuantity() < qty)
                throw new InsufficientStockException("Insufficient stock for product id " + product.getProductId());

            // decrement stock
            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(qty);
            BigDecimal unitPrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(qty)));
            orderItem.setOrder(order);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);
        order.setOrderStatus(getDefaultOrderStatus());
        order.setPaymentStatus(getDefaultPaymentStatus());

        Order savedOrder = orderRepository.save(order);

        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        return savedOrder;
    }

    @Transactional
    public Order createOrder(OrderRequestDto req, MultipartFile slipFile) {
        if (req == null) throw new IllegalArgumentException("Checkout request cannot be null");

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setCustomerName(req.getCustomerName());
        orderRequestDto.setCustomerEmail(req.getCustomerEmail());
        orderRequestDto.setCustomerAddress(req.getCustomerAddress());
        orderRequestDto.setTelephoneNumber(req.getTelephoneNumber());

        List<OrderItemRequestDto> items = new ArrayList<>();
        if (req.getItems() != null) {
            for (OrderItemRequestDto dto : req.getItems()) {
                OrderItemRequestDto item = new OrderItemRequestDto();
                item.setProductId(dto.getProductId());
                item.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
                item.setUnitPrice(dto.getUnitPrice());
                items.add(item);
            }
        }
        orderRequestDto.setItems(items);

        Order order = createOrder(orderRequestDto);

        // Handle slip upload
        if (slipFile != null && !slipFile.isEmpty()) {
            uploadSlip(order.getOrderId(), slipFile);
        }

        return order;
    }


    // ---------------- Slip Handling ----------------

    @Transactional
    public Slip uploadSlip(Long orderId, MultipartFile file) {
        Order order = getOrder(orderId);
        String subdir = "orders/" + order.getOrderId();
        String relativePath = fileStorageService.storeFile(file, subdir);

        // Remove previous slip
        Slip existing = slipRepository.findByOrder_OrderId(orderId).orElse(null);
        if (existing != null) {
            if (existing.getFilePath() != null) fileStorageService.delete(existing.getFilePath());
            slipRepository.delete(existing);
        }

        Slip slip = new Slip();
        slip.setOrder(order);
        slip.setFileName(file.getOriginalFilename());
        slip.setFilePath(relativePath);
        slip.setFileType(file.getContentType());
        slip.setFileSize(file.getSize());
        slip.setUploadedAt(LocalDateTime.now());

        Slip savedSlip = slipRepository.save(slip);

        order.addSlip(savedSlip);
        order.setOrderStatus(getSlipUploadedStatus());
        orderRepository.save(order);

        return savedSlip;
    }

    @Transactional
    public Slip replaceSlip(Long orderId, MultipartFile file) {
        return uploadSlip(orderId, file);
    }

    @Transactional
    public void deleteSlip(Long orderId) {
        Order order = getOrder(orderId);
        Slip existing = slipRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Slip not found for order id: " + orderId));
        if (existing.getFilePath() != null) fileStorageService.delete(existing.getFilePath());
        slipRepository.delete(existing);
        order.removeSlip(existing);
        order.setOrderStatus(getDefaultOrderStatus());
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Slip getSlip(Long orderId) {
        return slipRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Slip not found for order id: " + orderId));
    }

    // ---------------- Order Retrieval & Update ----------------

    @Transactional(readOnly = true)
    public List<Order> listAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
    }

    @Transactional
    public Order updateStatuses(Long orderId, String orderStatusStr, String paymentStatusStr) {
        Order order = getOrder(orderId); // Fetch the order

        // Update OrderStatus
        if (orderStatusStr != null && !orderStatusStr.isBlank()) {
            try {
                // Convert string to the Enum type
                OrderStatusType.OrderStatus osEnum = OrderStatusType.OrderStatus.valueOf(orderStatusStr.toLowerCase());

                // Find the existing status entity in the database
                OrderStatusType statusType = orderStatusTypeRepository.findByOrderStatusName(osEnum)
                        .orElseThrow(() -> new IllegalArgumentException("Order status type not found in database: " + orderStatusStr));

                // Assign the fetched entity to the order
                order.setOrderStatus(statusType);

            } catch (IllegalArgumentException e) {
                // Handle invalid string input OR status not found in DB
                throw new IllegalArgumentException("Invalid or unknown order status: " + orderStatusStr, e);
            }
        }

        // Update PaymentStatus
        if (paymentStatusStr != null && !paymentStatusStr.isBlank()) {
            try {
                // Convert string to the Enum type
                PaymentStatusType.PaymentStatus psEnum = PaymentStatusType.PaymentStatus.valueOf(paymentStatusStr.toLowerCase());

                // Find the existing status entity in the database
                PaymentStatusType paymentStatus = paymentStatusTypeRepository.findByPaymentStatusName(psEnum)
                        .orElseThrow(() -> new IllegalArgumentException("Payment status type not found in database: " + paymentStatusStr));

                // Assign the fetched entity to the order
                order.setPaymentStatus(paymentStatus);

                // --- Optional: Update Slip status when Payment Status changes ---
                // If payment is verified, also mark the associated slip as verified
                if (psEnum == PaymentStatusType.PaymentStatus.verified && order.getSlips() != null && !order.getSlips().isEmpty()) {
                    Slip slip = order.getSlips().get(0); // Assuming one slip per order for now
                    if (!slip.getVerified()) { // Check if not already verified
                        slip.setPaymentStatus(paymentStatus); // Update slip's status link
                        slip.markAsVerified(); // Set verified flag and timestamp
                        slipRepository.save(slip); // Save the updated slip
                    }
                }
                // Add similar logic for 'refunded' or 'failed' if needed
                // --- End Optional Slip Update ---

            } catch (IllegalArgumentException e) {
                // Handle invalid string input OR status not found in DB
                throw new IllegalArgumentException("Invalid or unknown payment status: " + paymentStatusStr, e);
            }
        }

        // Save the updated order with the correct status references
        return orderRepository.save(order);
    }


    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getOrderStatus() != null &&
                (order.getOrderStatus().getOrderStatusName() == OrderStatusType.OrderStatus.verified
                        || order.getOrderStatus().getOrderStatusName() == OrderStatusType.OrderStatus.paid)) {
            throw new IllegalArgumentException("Cannot cancel verified/paid order");
        }

        // Restock products
        for (OrderItem item : new ArrayList<>(order.getOrderItems())) {
            Product p = productRepository.findById(item.getProduct().getProductId()).orElse(null);
            if (p != null) {
                p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                productRepository.save(p);
            }
        }

        order.setOrderStatus(getCancelledStatus());
        return orderRepository.save(order);
    }

    // ---------------- Helper methods ----------------

    private OrderStatusType getDefaultOrderStatus() {
        return new OrderStatusType(null, OrderStatusType.OrderStatus.pending);
    }

    private OrderStatusType getCancelledStatus() {
        return new OrderStatusType(null, OrderStatusType.OrderStatus.cancelled);
    }

    private OrderStatusType getSlipUploadedStatus() {
        return new OrderStatusType(null, OrderStatusType.OrderStatus.processing);
    }

    private PaymentStatusType getDefaultPaymentStatus() {
        PaymentStatusType status = new PaymentStatusType();
        status.setPaymentStatusName(PaymentStatusType.PaymentStatus.pending);
        return status;
    }

}
