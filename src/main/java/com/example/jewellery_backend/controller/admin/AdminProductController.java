package com.example.jewellery_backend.controller.admin;

import com.example.jewellery_backend.dto.CreateUpdateProductRequest;
import com.example.jewellery_backend.dto.ProductDto;
import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    // ------------------ Product Endpoints ------------------

    /**
     * List all products
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDto>> list() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId) {
        ProductDto product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * Create a new product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> create(@RequestBody CreateUpdateProductRequest req) {
        ProductDto created = productService.createProduct(req);
        return ResponseEntity
                .created(URI.create("/admin/products/" + created.getProductId()))
                .body(created);
    }

    /**
     * Update an existing product
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> update(@PathVariable Long productId,
                                             @RequestBody CreateUpdateProductRequest req) {
        ProductDto updated = productService.updateProduct(productId, req);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a product
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
