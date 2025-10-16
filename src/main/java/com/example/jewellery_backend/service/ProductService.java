package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.Filter.FilterRequest;
import com.example.jewellery_backend.spec.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;


import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.entity.GoldRate;
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
    public Optional<Product> getProductById(Long productid) {
        return productRepository.findById(productid);
    }

    // Save a new product
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Update an existing product
    public Product updateProduct(Long productId, Product updatedProduct) {
        return productRepository.findById(productId).map(product -> {
            product.setProductName(updatedProduct.getProductName());
            product.setSku(updatedProduct.getSku());
            product.setBasePrice(updatedProduct.getBasePrice());
            product.setMarkupPercentage(updatedProduct.getMarkupPercentage());
            product.setDescription(updatedProduct.getDescription());
            product.setWeight(updatedProduct.getWeight());
            product.setDimensions(updatedProduct.getDimensions());
            product.setStockQuantity(updatedProduct.getStockQuantity());
            product.setMinStockLevel(updatedProduct.getMinStockLevel());
            product.setIsActive(updatedProduct.getIsActive());
            product.setFeatured(updatedProduct.getFeatured());
            product.setIsGold(updatedProduct.getIsGold());
            product.setGoldWeightGrams(updatedProduct.getGoldWeightGrams());
            product.setGoldPurityKarat(updatedProduct.getGoldPurityKarat());
            product.setImages(updatedProduct.getImages());
            product.setProductCategories(updatedProduct.getProductCategories());
            product.setAttributeValues(updatedProduct.getAttributeValues());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with ID " + productId));
    }

    // Delete a product
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    /**
     * Calculate updated price for a product:
     * 1. Start with basePrice (fail-safe to ZERO if null)
     * 2. Apply markupPercentage (if present) => base + base * (markupPercentage / 100)
     * 3. If product.isGold == true and gold weight exists, add (goldWeightGrams * latestGoldRate.price)
     *
     * Assumes GoldRate.price is price per gram (adjust if your GoldRate uses a different unit).
     */
    public BigDecimal calculateUpdatedPrice(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID " + productId));

        GoldRate goldRate = goldRateRepository.findTopByOrderByDateDesc()
                .orElseThrow(() -> new RuntimeException("Gold rate not found"));

        // safe defaults
        BigDecimal basePrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
        BigDecimal markupPct = product.getMarkupPercentage() != null ? product.getMarkupPercentage() : BigDecimal.ZERO;

        // Apply markup: base + base * (markupPct / 100)
        BigDecimal markupAmount = BigDecimal.ZERO;
        if (markupPct.compareTo(BigDecimal.ZERO) != 0) {
            markupAmount = basePrice.multiply(markupPct).divide(BigDecimal.valueOf(100));
        }
        BigDecimal finalPrice = basePrice.add(markupAmount);

        // If product has gold content, add goldWeightGrams * latest gold rate (price per gram)
        if (Boolean.TRUE.equals(product.getIsGold())) {
            BigDecimal goldWeight = product.getGoldWeightGrams() != null ? product.getGoldWeightGrams() : BigDecimal.ZERO;
            if (goldWeight.compareTo(BigDecimal.ZERO) > 0) {
                goldRate = goldRateRepository.findTopByOrderByDateDesc()
                        .orElseThrow(() -> new RuntimeException("Gold rate not found"));
                BigDecimal ratePerGram = goldRate.getRate() != null ? goldRate.getRate() : BigDecimal.ZERO;
                BigDecimal goldAmount = goldWeight.multiply(ratePerGram);
                finalPrice = finalPrice.add(goldAmount);
            }
        }

        return finalPrice;
    }

    /**
     * Filter products using provided FilterRequest.
     * Note: sort by 'productId' because Product entity maps primary key to productId.
     * ProductSpecification.filterBy(...) is assumed to accept the min/max/category/attributes parameters.
     */
    public Page<Product> filterProducts(FilterRequest filterRequest) {
        Double minPrice = filterRequest.getMinPrice();
        Double maxPrice = filterRequest.getMaxPrice();
        var categoryIds = filterRequest.getCategoryIds();
        var attributes = filterRequest.getAttributes();
        int page = Optional.ofNullable(filterRequest.getPage()).orElse(0);
        int size = Optional.ofNullable(filterRequest.getSize()).orElse(20);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "productId"));

        var spec = ProductSpecification.filterBy(minPrice, maxPrice, categoryIds, attributes);
        return productRepository.findAll(spec, pageable);
    }

}
