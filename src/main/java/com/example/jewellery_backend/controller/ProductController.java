package com.example.jewellery_backend.controller;

import com.example.jewellery_backend.dto.CreateUpdateProductRequest;
import com.example.jewellery_backend.dto.ProductDto;
import com.example.jewellery_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/products")

public class ProductController {

    private final ProductService productService;

    // All roles can view products
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> list = productService.getAllProducts();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto dto = productService.getProductById(id);
        if (dto == null) {
            throw new RuntimeException("Product not found with ID " + id);
        }
        return ResponseEntity.ok(dto);
    }

    // Only ADMIN and SUPER_ADMIN can add products
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody CreateUpdateProductRequest request) {
        ProductDto created = productService.createProduct(request);
        return ResponseEntity.ok(created);
    }

    // Only ADMIN and SUPER_ADMIN can update products
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody CreateUpdateProductRequest request) {
        ProductDto updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }

    // Only ADMIN and SUPER_ADMIN can delete products
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product with ID " + id + " deleted successfully.");
    }

    // All roles can view updated price
    @GetMapping("/{id}/updated-price")
    public ResponseEntity<BigDecimal> getUpdatedPrice(@PathVariable Long id) {
        // prefer service to calculate; see suggested service addition below
        BigDecimal updated = productService.getUpdatedPrice(id);
        return ResponseEntity.ok(updated);
    }
}
