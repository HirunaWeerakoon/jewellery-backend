package com.example.jewellery_backend.controller.admin;

import com.example.jewellery_backend.dto.CreateUpdateProductRequest;
import com.example.jewellery_backend.dto.ProductDto;
import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.repository.ProductRepository;
import com.example.jewellery_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductRepository productRepository;
    private final ProductService productService; // <-- instance


    // ------------------ Product Endpoints ------------------

    @GetMapping
    public ResponseEntity<List<ProductDto>> list() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Get product by ID
    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        return productRepository.findById(productId).orElseThrow();
    }

    // Add a new product
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> create(@RequestBody CreateUpdateProductRequest req) {
        ProductDto created = productService.createProduct(req);
        return ResponseEntity.created(URI.create("/api/admin/products/" + created.getProductId())).body(created);
    }

    // Update a product
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> update(@PathVariable Long id, @RequestBody CreateUpdateProductRequest req) {
        ProductDto updated = productService.updateProduct(id, req);
        return ResponseEntity.ok(updated);
    }

    // Delete a product
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------ Utility Methods ------------------

    // Example of correct getter usage
    public Long getProductIdExample(Product product) {
        return product.getProductId();
    }
}
