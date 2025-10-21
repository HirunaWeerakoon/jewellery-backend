package com.example.jewellery_backend.service;
import com.example.jewellery_backend.repository.OrderStatusTypeRepository;
import com.example.jewellery_backend.repository.PaymentStatusTypeRepository;
import com.example.jewellery_backend.dto.OrderItemRequestDto;
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
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import java.util.Objects;

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
        Order order = new Order();
        order.setUserName(customerDetails.getCustomerName());
        order.setUserEmail(customerDetails.getCustomerEmail());
        order.setUserAddress(customerDetails.getCustomerAddress());
        order.setTelephoneNumber(customerDetails.getTelephoneNumber());
        order.setCreatedAt(LocalDateTime.now());
        // Remove CartHeader link later if desired (Step 3 below)

        BigDecimal subTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 3. Process Cart Items -> OrderItems (and update stock)
        for (CartItem cartItem : cart.getItemList()) {
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

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(qty);
            BigDecimal unitPrice = cartItem.getUnitPrice() != null ? cartItem.getUnitPrice() : BigDecimal.ZERO;
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(qty)));
            // orderItem.setOrder(order); // Will be set after order is saved

            orderItems.add(orderItem);
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
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
            savedOrder.getOrderItems().add(item); // Add to managed list
        }

        // 6. Store Slip file and create Slip entity
        String subdir = "slips/order_" + savedOrder.getOrderId();
        String relativePath = fileStorageService.storeFile(slipFile, subdir);

        Slip slip = new Slip();
        slip.setOrder(savedOrder);
        slip.setFileName(StringUtils.cleanPath(Objects.requireNonNull(slipFile.getOriginalFilename())));
        slip.setFilePath(relativePath);
        slip.setFileType(slipFile.getContentType());
        slip.setFileSize(slipFile.getSize());
        slip.setUploadedAt(LocalDateTime.now());
        slip.setPaymentStatus(pendingPaymentStatus); // Assign the fetched pending status
        slip.setVerified(false);

        Slip savedSlip = slipRepository.save(slip);
        savedOrder.getSlips().add(savedSlip); // Add to managed list

        // 7. Clear the session cart
        session.removeAttribute(Cart.SESSION_ATTRIBUTE);

        return savedOrder;
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
        Order order = getOrder(orderId);
        Slip existing = slipRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Slip not found for order id: " + orderId));
        if (existing.getFilePath() != null) fileStorageService.delete(existing.getFilePath());
        slipRepository.delete(existing);
        order.removeSlip(existing);
        // Fetch the 'pending' OrderStatusType from the database
        OrderStatusType pendingOrderStatus = orderStatusTypeRepository.findByOrderStatusName(OrderStatusType.OrderStatus.pending)
                .orElseThrow(() -> new IllegalStateException("Default 'pending' order status not found in database!"));
        order.setOrderStatus(pendingOrderStatus); // Assign the fetched entity
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

        // Fetch the 'cancelled' OrderStatusType from the database
        OrderStatusType cancelledOrderStatus = orderStatusTypeRepository.findByOrderStatusName(OrderStatusType.OrderStatus.cancelled)
                .orElseThrow(() -> new IllegalStateException("'cancelled' order status not found in database!"));
        order.setOrderStatus(cancelledOrderStatus); // Assign the fetched entity
        return orderRepository.save(order);
    }


}
