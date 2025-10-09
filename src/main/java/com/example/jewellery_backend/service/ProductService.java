package com.example.jewellery_backend.service;

import com.example.jewellery_backend.model.Product;
import com.example.jewellery_backend.model.GoldRate;
import com.example.jewellery_backend.repository.ProductRepository;
import com.example.jewellery_backend.repository.GoldRateRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final GoldRateRepository goldRateRepository;

    public ProductService(ProductRepository productRepository, GoldRateRepository goldRateRepository) {
        this.productRepository = productRepository;
        this.goldRateRepository = goldRateRepository;
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get a product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Save a new product
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Update an existing product
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setPrice(updatedProduct.getPrice());
            product.setDescription(updatedProduct.getDescription());
            product.setImageUrl(updatedProduct.getImageUrl());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with ID " + id));
    }

    // Delete a product
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Calculate updated price based on gold rate
    public BigDecimal calculateUpdatedPrice(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID " + productId));

        GoldRate goldRate = goldRateRepository.findTopByOrderByDateDesc()
                .orElseThrow(() -> new RuntimeException("Gold rate not found"));

        BigDecimal rate = goldRate.getPrice();

        return product.getPrice().multiply(rate);
    }
}
