package com.example.jewellery_backend.service;
import com.example.jewellery_backend.repository.OrderStatusTypeRepository;
import com.example.jewellery_backend.repository.PaymentStatusTypeRepository;
import com.example.jewellery_backend.dto.OrderRequestDto;
import com.example.jewellery_backend.entity.*;
import com.example.jewellery_backend.exception.InsufficientStockException;
import com.example.jewellery_backend.exception.ResourceNotFoundException;
import com.example.jewellery_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import com.example.jewellery_backend.Cart;
import com.example.jewellery_backend.CartItem;
import com.example.jewellery_backend.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import java.util.Objects;
import com.example.jewellery_backend.dto.OrderResponseDto;
import com.example.jewellery_backend.util.Mapper;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final SlipRepository slipRepository;
    private final FileStorageService fileStorageService;
    private final OrderStatusTypeRepository orderStatusTypeRepository;
    private final PaymentStatusTypeRepository paymentStatusTypeRepository;
    private final ProductService productService;


    @Transactional
    public Order createOrderFromSessionCart(OrderRequestDto customerDetails, MultipartFile slipFile, HttpSession session) {

        // 1. Validate inputs (Slip is required)
        if (slipFile == null || slipFile.isEmpty()) {
            throw new IllegalArgumentException("Payment slip is required.");
        }
        if (customerDetails == null) {
            throw new IllegalArgumentException("Customer details are required.");
        }

        // 2. Create Order entity and set customer details
        Order order = Order.builder()
                .userName(customerDetails.getCustomerName())
                .userEmail(customerDetails.getCustomerEmail())
                .userAddress(customerDetails.getCustomerAddress())
                .telephoneNumber(customerDetails.getTelephoneNumber())
                .orderItems(new ArrayList<>()) // Initialize collections
                .slips(new ArrayList<>())
                .subtotal(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .shippingAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .currency("LKR")
                .build();

        // 3. Fetch default statuses and save Order
        OrderStatusType pendingOrderStatus = orderStatusTypeRepository.findByOrderStatusName(OrderStatusType.OrderStatus.pending)
                .orElseThrow(() -> new IllegalStateException("Default 'pending' order status not found in database!"));
        order.setOrderStatus(pendingOrderStatus);

        PaymentStatusType pendingPaymentStatus = paymentStatusTypeRepository.findByPaymentStatusName(PaymentStatusType.PaymentStatus.pending)
                .orElseThrow(() -> new IllegalStateException("Default 'pending' payment status not found in database!"));
        order.setPaymentStatus(pendingPaymentStatus);

        Order savedOrder = orderRepository.save(order);

        // 4. Store Slip file and create Slip entity
        String subdir = "slips/order_" + savedOrder.getOrderId();
        String relativePath = fileStorageService.storeFile(slipFile, subdir);

        Slip slip = Slip.builder()
                .order(savedOrder)
                .fileName(StringUtils.cleanPath(Objects.requireNonNull(slipFile.getOriginalFilename())))
                .filePath(relativePath)
                .fileType(slipFile.getContentType())
                .fileSize(slipFile.getSize())
                .paymentStatus(pendingPaymentStatus) // Link to pending status
                .verified(false)
                .build();

        Slip savedSlip = slipRepository.save(slip);
        savedOrder.getSlips().add(savedSlip);

     ;

        System.out.println("DEMO FIX: Created Order ID " + savedOrder.getOrderId() + " without using session cart.");

        return savedOrder;

    }

    /*
    // ---------------- Create Order (Admin or Checkout) ----------------
    @Transactional
    public Order createOrderFromSessionCart(OrderRequestDto customerDetails, MultipartFile slipFile, HttpSession session) {
        // 1. Get Cart from Session
        Cart cart = (Cart) session.getAttribute(Cart.SESSION_ATTRIBUTE);
        if (cart == null || cart.getItemList() == null || cart.getItemList().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order with an empty cart.");
        }
        if (slipFile == null || slipFile.isEmpty()) {
            throw new IllegalArgumentException("Payment slip is required.");
        }

        // 2. Create Order entity and set customer details
        Order order = Order.builder()
                .userName(customerDetails.getCustomerName())
                .userEmail(customerDetails.getCustomerEmail())
                .userAddress(customerDetails.getCustomerAddress())
                .telephoneNumber(customerDetails.getTelephoneNumber())
                // createdAt/updatedAt handled by @PrePersist/@PreUpdate
                .orderItems(new ArrayList<>()) // Initialize collections
                .slips(new ArrayList<>())
                .build();

        BigDecimal subTotal = BigDecimal.ZERO;
        List<OrderItem> orderItemsTransient = new ArrayList<>();

        // 3. Process Cart Items -> OrderItems (and update stock)
        for (com.example.jewellery_backend.CartItem cartItem : cart.getItemList()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart with id: " + cartItem.getProductId()));

            int qty = cartItem.getQuantity();
            if (qty <= 0) continue; // Skip invalid items

            if (product.getStockQuantity() < qty) {
                throw new InsufficientStockException("Insufficient stock for product id " + product.getProductId() + " (" + product.getProductName() + ")");
            }

            // Decrement stock
            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepository.save(product);
            BigDecimal unitPrice = productService.getUpdatedPrice(product.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(qty)
                    .unitPrice(unitPrice) // Use calculated price
                    // .order(order) // Set after order is saved
                    .build();
            orderItem.calculateTotalPrice(); // Calculate total based on unitPrice and quantity

            orderItemsTransient.add(orderItem);
            subTotal = subTotal.add(orderItem.getTotalPrice());
        }

        // 4. Set totals, FETCH default statuses, and save Order
        order.setOrderItems(new ArrayList<>()); // Initialize collection
        order.setSubtotal(subTotal);
        order.setTaxAmount(BigDecimal.ZERO); // Example
        order.setShippingAmount(BigDecimal.ZERO); // Example
        order.setDiscountAmount(BigDecimal.ZERO); // Example
        order.setTotalAmount(subTotal.add(order.getTaxAmount()).add(order.getShippingAmount()).subtract(order.getDiscountAmount()));

        // Fetch the 'pending' OrderStatusType from the database
        OrderStatusType pendingOrderStatus = orderStatusTypeRepository.findByOrderStatusName(OrderStatusType.OrderStatus.pending)
                .orElseThrow(() -> new IllegalStateException("Default 'pending' order status not found in database!"));
        order.setOrderStatus(pendingOrderStatus); // Assign the fetched entity

        // Fetch the 'pending' PaymentStatusType from the database
        PaymentStatusType pendingPaymentStatus = paymentStatusTypeRepository.findByPaymentStatusName(PaymentStatusType.PaymentStatus.pending)
                .orElseThrow(() -> new IllegalStateException("Default 'pending' payment status not found in database!"));
        order.setPaymentStatus(pendingPaymentStatus); // Assign the fetched entity

        Order savedOrder = orderRepository.save(order); // Save Order first

        // 5. Link and save OrderItems
        for (OrderItem item : orderItemsTransient) {
            item.setOrder(savedOrder); // Link to saved order
            orderItemRepository.save(item); // Save item
            savedOrder.getOrderItems().add(item); // Add to managed collection
        }

        // 6. Store Slip file and create Slip entity
        String subdir = "slips/order_" + savedOrder.getOrderId();
        String relativePath = fileStorageService.storeFile(slipFile, subdir);

        Slip slip = Slip.builder()
                .order(savedOrder)
                .fileName(StringUtils.cleanPath(Objects.requireNonNull(slipFile.getOriginalFilename())))
                .filePath(relativePath)
                .fileType(slipFile.getContentType())
                .fileSize(slipFile.getSize())
                .paymentStatus(pendingPaymentStatus) // Link to pending status
                .verified(false)
                // uploadedAt handled by @Builder.Default
                .build();

        Slip savedSlip = slipRepository.save(slip);
        savedOrder.getSlips().add(savedSlip);

        // 7. Clear the session cart
        session.removeAttribute(Cart.SESSION_ATTRIBUTE);

        return savedOrder;
    }*/



    // ---------------- Slip Handling ----------------

    @Transactional
    public Slip uploadSlip(Long orderId, MultipartFile file) {
        Order order = getOrder(orderId);
        String subdir = "slips/order_" + order.getOrderId();
        String relativePath = fileStorageService.storeFile(file, subdir);

        // Remove previous slip if it exists
        Optional<Slip> existingOpt = slipRepository.findByOrder_OrderId(orderId);
        if (existingOpt.isPresent()) {
            Slip existing = existingOpt.get();
            if (existing.getFilePath() != null) {
                fileStorageService.delete(existing.getFilePath());
            }
            // Remove the slip from the order's collection before deleting
            if (order.getSlips() != null) {
                // Use iterator to avoid ConcurrentModificationException
                Iterator<Slip> iterator = order.getSlips().iterator();
                while (iterator.hasNext()) {
                    Slip s = iterator.next();
                    if (s.getSlipId().equals(existing.getSlipId())) {
                        iterator.remove();
                        s.setOrder(null); // Break bidirectional link
                        break;
                    }
                }
            }
            slipRepository.delete(existing); // Now delete the orphan slip
        }

        Slip slip = Slip.builder() // Use builder
                .order(order) // Link to order
                .fileName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())))
                .filePath(relativePath)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                // uploadedAt handled by @Builder.Default
                .verified(false) // Default to not verified
                .build();
        PaymentStatusType initialPaymentStatus = paymentStatusTypeRepository.findByPaymentStatusName(PaymentStatusType.PaymentStatus.pending)
                .orElseThrow(() -> new IllegalStateException("Default 'pending' payment status not found!"));
        slip.setPaymentStatus(initialPaymentStatus);

        Slip savedSlip = slipRepository.save(slip);

        order.addSlip(savedSlip);
        // Add the new slip to the order's collection
        if (order.getSlips() == null) {
            order.setSlips(new ArrayList<>());
        }
        order.getSlips().add(savedSlip);
        // Fetch the 'processing' OrderStatusType from the database
        OrderStatusType processingOrderStatus = orderStatusTypeRepository.findByOrderStatusName(OrderStatusType.OrderStatus.processing)
                .orElseThrow(() -> new IllegalStateException("'processing' order status not found in database!"));
        order.setOrderStatus(processingOrderStatus); // Assign the fetched entity
        orderRepository.save(order);

        return savedSlip;
    }

    @Transactional
    public Slip replaceSlip(Long orderId, MultipartFile file) {
        return uploadSlip(orderId, file);
    }

    @Transactional
    public void deleteSlip(Long orderId) {
        Order order = getOrder(orderId); // Ensures order exists

        Optional<Slip> existingOpt = slipRepository.findByOrder_OrderId(orderId);
        if (existingOpt.isPresent()) {
            Slip existing = existingOpt.get();
            if (existing.getFilePath() != null) {
                fileStorageService.delete(existing.getFilePath()); // Delete file from storage
            }
            // Remove from order's collection first
            if (order.getSlips() != null) {
                Iterator<Slip> iterator = order.getSlips().iterator();
                while (iterator.hasNext()) {
                    Slip s = iterator.next();
                    if (s.getSlipId().equals(existing.getSlipId())) {
                        iterator.remove();
                        s.setOrder(null); // Break link
                        break;
                    }
                }
            }
            slipRepository.delete(existing); // Delete from DB

            // Optionally revert order status if slip deletion means order goes back to pending
            OrderStatusType pendingOrderStatus = orderStatusTypeRepository.findByOrderStatusName(OrderStatusType.OrderStatus.pending)
                    .orElseThrow(() -> new IllegalStateException("Default 'pending' order status not found!"));
            order.setOrderStatus(pendingOrderStatus);
            orderRepository.save(order); // Save updated order status

        } else {
            throw new ResourceNotFoundException("Slip not found for order id: " + orderId);
        }
    }

    @Transactional(readOnly = true)
    public Slip getSlip(Long orderId) {
        return slipRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Slip not found for order id: " + orderId));
    }

    // ---------------- Order Retrieval & Update ----------------

    @Transactional(readOnly = true)
    public List<OrderResponseDto> listAllOrders() {
        List<Order> orders = orderRepository.findAll();
        // Map to DTOs *inside* the transactional method
        return orders.stream()
                .map(Mapper::toOrderResponseDto)
                .collect(Collectors.toList());
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
                OrderStatusType.OrderStatus osEnum = OrderStatusType.OrderStatus.valueOf(orderStatusStr.toLowerCase());
                OrderStatusType statusType = orderStatusTypeRepository.findByOrderStatusName(osEnum)
                        .orElseThrow(() -> new IllegalArgumentException("Order status type not found: " + orderStatusStr));
                order.setOrderStatus(statusType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid order status: " + orderStatusStr, e);
            }
        }

        // Update PaymentStatus
        if (paymentStatusStr != null && !paymentStatusStr.isBlank()) {
            try {
                PaymentStatusType.PaymentStatus psEnum = PaymentStatusType.PaymentStatus.valueOf(paymentStatusStr.toLowerCase());
                PaymentStatusType paymentStatus = paymentStatusTypeRepository.findByPaymentStatusName(psEnum)
                        .orElseThrow(() -> new IllegalArgumentException("Payment status type not found: " + paymentStatusStr));
                order.setPaymentStatus(paymentStatus);

                // Update associated Slip status and verification
                Optional<Slip> slipOpt = slipRepository.findByOrder_OrderId(orderId);
                if (slipOpt.isPresent()) {
                    Slip slip = slipOpt.get();
                    boolean slipUpdated = false;
                    // Mark verified if payment status is verified and slip isn't already
                    if (psEnum == PaymentStatusType.PaymentStatus.verified && !Boolean.TRUE.equals(slip.getVerified())) {
                        slip.setPaymentStatus(paymentStatus);
                        slip.markAsVerified();
                        slipUpdated = true;
                    }
                    // Update slip status if payment moves to refunded/failed
                    else if ((psEnum == PaymentStatusType.PaymentStatus.refunded || psEnum == PaymentStatusType.PaymentStatus.failed)
                            && !slip.getPaymentStatus().getPaymentStatusName().equals(psEnum)) {
                        slip.setPaymentStatus(paymentStatus);
                        // Optionally un-verify if logic requires it
                        // if (psEnum == PaymentStatusType.PaymentStatus.refunded && Boolean.TRUE.equals(slip.getVerified())) {
                        //     slip.setVerified(false);
                        //     slip.setVerifiedAt(null);
                        // }
                        slipUpdated = true;
                    }

                    if (slipUpdated) {
                        slipRepository.save(slip);
                    }
                }

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid payment status: " + paymentStatusStr, e);
            }
        }

        return orderRepository.save(order); // Save updated order
    }


    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);

        OrderStatusType currentStatus = order.getOrderStatus();
        if (currentStatus == null) {
            throw new IllegalStateException("Order status is null for order ID: " + orderId);
        }

        // Define non-cancellable statuses clearly
        Set<OrderStatusType.OrderStatus> nonCancellable = EnumSet.of(
                OrderStatusType.OrderStatus.shipped,
                OrderStatusType.OrderStatus.delivered,
                OrderStatusType.OrderStatus.cancelled,
                OrderStatusType.OrderStatus.refunded
        );

        if (nonCancellable.contains(currentStatus.getOrderStatusName())) {
            throw new IllegalArgumentException("Cannot cancel order with status: " + currentStatus.getOrderStatusName());
        }

        // Restock products
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (item != null && item.getProduct() != null) {
                    // Use findById for better error handling if product deleted concurrently
                    Optional<Product> pOpt = productRepository.findById(item.getProduct().getProductId());
                    if (pOpt.isPresent()) {
                        Product p = pOpt.get();
                        p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                        // Let transaction manage saving
                    } else {
                        System.err.println("Warning: Product ID " + item.getProduct().getProductId() + " not found during restocking for cancelled order ID " + orderId);
                    }
                }
            }
        }

        // Fetch and set 'cancelled' status
        OrderStatusType cancelledOrderStatus = orderStatusTypeRepository.findByOrderStatusName(OrderStatusType.OrderStatus.cancelled)
                .orElseThrow(() -> new IllegalStateException("'cancelled' order status not found!"));
        order.setOrderStatus(cancelledOrderStatus);

        // Transaction commit will save order status and product stock changes
        return order; // Return the order in its cancelled state
    }

}
