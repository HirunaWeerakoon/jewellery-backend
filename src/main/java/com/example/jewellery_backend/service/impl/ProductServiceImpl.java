package com.example.jewellery_backend.service.impl;

import com.example.jewellery_backend.dto.CreateUpdateProductRequest;
import com.example.jewellery_backend.dto.ProductDto;
import com.example.jewellery_backend.dto.ProductCategoryDto;
import com.example.jewellery_backend.entity.*;
import com.example.jewellery_backend.exception.ResourceNotFoundException;
import com.example.jewellery_backend.repository.ProductRepository;
import com.example.jewellery_backend.repository.CategoryRepository;
import com.example.jewellery_backend.repository.ProductCategoryRepository;
import com.example.jewellery_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    // ---------------- Mapping methods ----------------

    private ProductDto toDto(Product p) {
        ProductDto dto = ProductDto.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .sku(p.getSku())
                .description(p.getDescription())
                .basePrice(p.getBasePrice())
                .markupPercentage(p.getMarkupPercentage())
                .weight(p.getWeight())
                .dimensions(p.getDimensions())
                .stockQuantity(p.getStockQuantity())
                .minStockLevel(p.getMinStockLevel())
                .isActive(p.getIsActive())
                .featured(p.getFeatured())
                .isGold(p.getIsGold())
                .goldWeightGrams(p.getGoldWeightGrams())
                .goldPurityKarat(p.getGoldPurityKarat())
                .build();

        if (p.getProductCategories() != null && !p.getProductCategories().isEmpty()) {
            List<ProductCategoryDto> categoryDtos = p.getProductCategories().stream()
                    .map(pc -> {
                        Category c = pc.getCategory();
                        return ProductCategoryDto.builder()
                                .categoryId(c.getCategoryId())
                                .categoryName(c.getCategoryName())
                                .categorySlug(c.getSlug())
                                .categoryIsActive(c.getIsActive())
                                .build();
                    })
                    .collect(Collectors.toList());
            dto.setProductCategories(categoryDtos);
        }

        return dto;
    }

    private void applyCategories(Product p, Set<Long> categoryIds) {
        if (p.getProductCategories() != null) {
            p.getProductCategories().clear();
        } else {
            p.setProductCategories(new ArrayList<>());
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long cid : categoryIds) {
                Category c = categoryRepository.findById(cid)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id", cid));

                ProductCategory pc = ProductCategory.builder()
                        .id(new ProductCategoryId(
                                p.getProductId() != null ? p.getProductId().intValue() : null,
                                c.getCategoryId() != null ? c.getCategoryId().intValue() : null
                        ))
                        .product(p)
                        .category(c)
                        .build();

                p.getProductCategories().add(pc);
            }
        }
    }

    // ---------------- CRUD methods ----------------

    @Override
    public ProductDto createProduct(CreateUpdateProductRequest req) {
        Product p = Product.builder()
                .productName(req.getProductName())
                .description(req.getDescription())
                .sku(req.getSku())
                .basePrice(req.getBasePrice())
                .markupPercentage(req.getMarkupPercentage())
                .weight(req.getWeight())
                .dimensions(req.getDimensions())
                .stockQuantity(req.getStockQuantity())
                .minStockLevel(req.getMinStockLevel())
                .isActive(req.getIsActive())
                .featured(req.getFeatured())
                .isGold(req.getIsGold())
                .goldWeightGrams(req.getGoldWeightGrams())
                .goldPurityKarat(req.getGoldPurityKarat())
                .build();

        Product saved = productRepository.save(p); // Save first to get ID

        applyCategories(saved, req.getCategoryIds());

        return toDto(productRepository.save(saved));
    }

    @Override
    public ProductDto updateProduct(Long id, CreateUpdateProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (req.getProductName() != null) p.setProductName(req.getProductName());
        p.setDescription(req.getDescription());
        p.setSku(req.getSku());
        p.setBasePrice(req.getBasePrice());
        p.setMarkupPercentage(req.getMarkupPercentage());
        p.setWeight(req.getWeight());
        p.setDimensions(req.getDimensions());
        p.setStockQuantity(req.getStockQuantity());
        p.setMinStockLevel(req.getMinStockLevel());
        p.setIsActive(req.getIsActive());
        p.setFeatured(req.getFeatured());
        p.setIsGold(req.getIsGold());
        p.setGoldWeightGrams(req.getGoldWeightGrams());
        p.setGoldPurityKarat(req.getGoldPurityKarat());

        applyCategories(p, req.getCategoryIds());

        return toDto(productRepository.save(p));
    }

    @Override
    public void deleteProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(p);
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDto(p);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getProductsByCategoryId(Long categoryId) {
        // Query productCategoryRepository since Category does not have productCategories
        List<ProductCategory> pcs = productCategoryRepository.findByCategoryId(categoryId);

        if (pcs != null && !pcs.isEmpty()) {
            return pcs.stream()
                    .map(ProductCategory::getProduct)
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
