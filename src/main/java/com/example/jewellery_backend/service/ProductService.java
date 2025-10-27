
package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.CreateUpdateProductRequest;
import com.example.jewellery_backend.dto.ProductDto;
import com.example.jewellery_backend.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    ProductDto createProduct(CreateUpdateProductRequest req);
    ProductDto updateProduct(Long id, CreateUpdateProductRequest req);
    void deleteProduct(Long id);
    ProductDto getProductById(Long id);
    List<ProductDto> getAllProducts();
    List<ProductDto> getProductsByCategoryId(Long categoryId);

    Product saveProduct(Product product);

    BigDecimal getUpdatedPrice(Long id);
    Optional<Product> findById(Long id);

    // New method to get only active (public-visible) products
    List<ProductDto> getActiveProducts();
}

