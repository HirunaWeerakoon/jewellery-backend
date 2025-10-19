package com.example.jewellery_backend.controller;

import com.example.jewellery_backend.dto.CategoryDto;
import com.example.jewellery_backend.dto.ProductDto;
import com.example.jewellery_backend.service.CategoryService;
import com.example.jewellery_backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public PublicController(CategoryService categoryService,
                            ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> allCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> allProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/categories/{id}/products")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable Long id) {
        List<ProductDto> products = productService.getProductsByCategoryId(id);
        return ResponseEntity.ok(products);
    }
}
