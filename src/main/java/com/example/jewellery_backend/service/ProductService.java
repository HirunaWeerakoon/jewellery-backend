package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.CreateUpdateProductRequest;
import com.example.jewellery_backend.dto.Filter.FilterRequest;
import com.example.jewellery_backend.dto.ProductDto;
import com.example.jewellery_backend.entity.Product;
import org.springframework.data.domain.Page;


import java.util.List;

public interface ProductService {
    ProductDto createProduct(CreateUpdateProductRequest req);
    ProductDto updateProduct(Long id, CreateUpdateProductRequest req);
    void deleteProduct(Long id);
    ProductDto getProductById(Long id);
    List<ProductDto> getAllProducts();
    List<ProductDto> getProductsByCategoryId(Long categoryId);
}
