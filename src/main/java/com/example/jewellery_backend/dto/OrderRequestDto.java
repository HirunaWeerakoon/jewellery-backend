package com.example.jewellery_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrderRequestDto {
    @NotNull
    private String customerName;

    @NotNull
    @Email
    private String customerEmail;

    // totalAmount may be validated/calculated server-side too (we will calculate from items)
    private Double totalAmount;

    @NotNull
    private List<OrderItemRequestDto> items;

    // optional - if you add more payment methods later make an enum
    private String paymentMethod = "BANK_TRANSFER";

    // getters + setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItemRequestDto> getItems() { return items; }
    public void setItems(List<OrderItemRequestDto> items) { this.items = items; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
