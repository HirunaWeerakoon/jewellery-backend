package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.OrderItemRequestDto;
import com.example.jewellery_backend.dto.OrderRequestDto;
import com.example.jewellery_backend.entity.Order;
import com.example.jewellery_backend.entity.OrderItem;
import com.example.jewellery_backend.entity.OrderStatus;
import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.entity.Slip;
import com.example.jewellery_backend.exception.InsufficientStockException;
import com.example.jewellery_backend.exception.ResourceNotFoundException;
import com.example.jewellery_backend.repository.OrderRepository;
import com.example.jewellery_backend.repository.ProductRepository;
import com.example.jewellery_backend.repository.SlipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OrderService
 * - createOrder: validates products, decrements stock, saves order + items
 * - slip management: upload/replace/delete/get
 * - find/get/update/cancel order helpers
 */
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
        order.setCustomerName(orderRequestDto.getCustomerName());
        order.setCustomerEmail(orderRequestDto.getCustomerEmail());

        double total = 0.0;

        for (OrderItemRequestDto itemReq : orderRequestDto.getItems()) {
            if (itemReq == null || itemReq.getProductId() == null) {
                throw new IllegalArgumentException("Each order item must contain a productId");
            }
            Long productId = itemReq.getProductId();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            Integer qty = itemReq.getQuantity();
            if (qty == null || qty <= 0) {
                throw new IllegalArgumentException("Quantity must be >= 1 for product id " + productId);
            }

            if (product.getStock() < qty) {
                throw new InsufficientStockException("Insufficient stock for product id " + productId);
            }

            // decrement product stock
            product.setStock(product.getStock() - qty);
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setProductId(productId);

            double unitPrice = itemReq.getUnitPrice() != null
                    ? itemReq.getUnitPrice()
                    : (product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);

            item.setUnitPrice(unitPrice);
            item.setQuantity(qty);
            double subtotal = unitPrice * qty;
            item.setSubtotal(subtotal);

            // addOrderItem ensures back-reference
            order.addOrderItem(item);
            total += subtotal;
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) {
            return findAll();
        }
        return orderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    // ---------- NEW METHOD ----------
    @Transactional(readOnly = true)
    public Slip getSlip(Long orderId) {
        Order order = getOrder(orderId);
        Slip slip = order.getSlip();
        if (slip == null) {
            throw new ResourceNotFoundException("Slip not found for order id: " + orderId);
        }
        return slip;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrder(orderId);
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public Slip uploadSlip(Long orderId, MultipartFile file) {
        Objects.requireNonNull(file, "File must be provided");
        Order order = getOrder(orderId);

        String subdir = "orders/" + order.getId();
        String relativePath = fileStorageService.storeFile(file, subdir);

        // remove previous slip if any
        Slip existing = slipRepository.findByOrderId(order.getId()).orElse(null);
        if (existing != null) {
            if (existing.getFilePath() != null) {
                fileStorageService.delete(existing.getFilePath());
            }
            slipRepository.delete(existing);
        }

        Slip slip = new Slip();
        slip.setOrder(order);
        slip.setFileName(file.getOriginalFilename());
        slip.setFilePath(relativePath);
        slip.setFileType(file.getContentType());
        slip.setFileSize(file.getSize());

        Slip saved = slipRepository.save(slip);
        order.setSlip(saved);
        order.setStatus(OrderStatus.SLIP_UPLOADED);
        orderRepository.save(order);
        return saved;
    }

    @Transactional
    public Slip replaceSlip(Long orderId, MultipartFile file) {
        return uploadSlip(orderId, file);
    }

    @Transactional
    public void deleteSlip(Long orderId) {
        Order order = getOrder(orderId);
        Slip existing = slipRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Slip not found for order id: " + orderId));

        if (existing.getFilePath() != null) {
            fileStorageService.delete(existing.getFilePath());
        }
        slipRepository.delete(existing);

        order.setSlip(null);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() == OrderStatus.VERIFIED || order.getStatus() == OrderStatus.PAID) {
            throw new IllegalArgumentException("Cannot cancel a verified/paid order");
        }

        // restock products
        if (order.getOrderItems() != null) {
            for (OrderItem item : new ArrayList<>(order.getOrderItems())) {
                Product p = productRepository.findById(item.getProductId()).orElse(null);
                if (p != null) {
                    p.setStock(p.getStock() + item.getQuantity());
                    productRepository.save(p);
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}
