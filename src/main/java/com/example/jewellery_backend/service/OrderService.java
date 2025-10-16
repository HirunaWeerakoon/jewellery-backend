package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.OrderItemRequestDto;
import com.example.jewellery_backend.dto.OrderRequestDto;
import com.example.jewellery_backend.entity.*;
import com.example.jewellery_backend.exception.InsufficientStockException;
import com.example.jewellery_backend.exception.ResourceNotFoundException;
import com.example.jewellery_backend.repository.OrderRepository;
import com.example.jewellery_backend.repository.ProductRepository;
import com.example.jewellery_backend.repository.SlipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SlipRepository slipRepository;
    private final FileStorageService fileStorageService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        SlipRepository slipRepository,
                        FileStorageService fileStorageService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.slipRepository = slipRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Order createOrder(OrderRequestDto orderRequestDto) {
        if (orderRequestDto == null) {
            throw new IllegalArgumentException("Order request cannot be null");
        }
        if (orderRequestDto.getItems() == null || orderRequestDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setUserName(orderRequestDto.getCustomerName());
        order.setUserEmail(orderRequestDto.getCustomerEmail());
        order.setUserAddress(orderRequestDto.getCustomerAddress());
        order.setTelephoneNumber(orderRequestDto.getTelephoneNumber());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDto itemReq : orderRequestDto.getItems()) {
            if (itemReq == null || itemReq.getProductId() == null) {
                throw new IllegalArgumentException("Each order item must contain a productId");
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            Integer qty = itemReq.getQuantity();
            if (qty == null || qty <= 0) {
                throw new IllegalArgumentException("Quantity must be >= 1 for product id " + itemReq.getProductId());
            }

            if (product.getStockQuantity() < qty) {
                throw new InsufficientStockException("Insufficient stock for product id " + itemReq.getProductId());
            }

            // decrement stock
            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepository.save(product);

            // create order item
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(qty);
            BigDecimal unitPrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(qty)));

            // add item to order
            order.addOrderItem(item);
            totalAmount = totalAmount.add(item.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);
        order.setOrderStatus(getDefaultOrderStatus());
        order.setPaymentStatus(getDefaultPaymentStatus());

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatusType status) {
        if (status == null) return findAll();
        return orderRepository.findByOrderStatus(status);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatusType newStatus) {
        Order order = getOrder(orderId);
        if (newStatus == null) throw new IllegalArgumentException("New status cannot be null");
        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public Slip uploadSlip(Long orderId, MultipartFile file) {
        Objects.requireNonNull(file, "File must be provided");
        Order order = getOrder(orderId);

        String subdir = "orders/" + order.getOrderId();
        String relativePath = fileStorageService.storeFile(file, subdir);

        // Remove previous slip if exists
        Slip existing = slipRepository.findByOrderOrderId(order.getOrderId()).orElse(null);
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

        Slip existing = slipRepository.findByOrderOrderId(order.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Slip not found for order id: " + orderId));

        if (existing.getFilePath() != null) fileStorageService.delete(existing.getFilePath());
        slipRepository.delete(existing);

        order.removeSlip(existing);
        order.setOrderStatus(getDefaultOrderStatus());
        orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);

        if (order.getOrderStatus() != null &&
                (order.getOrderStatus().getOrderStatusName() == OrderStatusType.OrderStatus.verified
                        || order.getOrderStatus().getOrderStatusName() == OrderStatusType.OrderStatus.paid)) {
            throw new IllegalArgumentException("Cannot cancel a verified/paid order");
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
        return new PaymentStatusType(); // implement default as needed
    }

    @Transactional(readOnly = true)
    public Slip getSlip(Long orderId) {
        Order order = getOrder(orderId);
        return slipRepository.findByOrderOrderId(order.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Slip not found for order id: " + orderId));
    }

}
