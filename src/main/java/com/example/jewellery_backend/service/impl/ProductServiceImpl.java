package com.example.jewellery_backend.service.impl;
import com.example.jewellery_backend.dto.ProductImageDto;
import com.example.jewellery_backend.dto.ProductAttributeValueDto;
import java.util.Collections;
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
import com.example.jewellery_backend.repository.CategoryClosureRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryClosureRepository categoryClosureRepository;

    // ---------------- Mapping methods ----------------

    private ProductDto toDto(Product p) {
        if (p == null) {
            return null; // Handle null product input
        }

        // --- Map basic fields ---
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
                .build(); // Initial build

        // --- Map Categories ---
        if (p.getProductCategories() != null && !p.getProductCategories().isEmpty()) {
            List<ProductCategoryDto> categoryDtos = p.getProductCategories().stream()
                    .filter(pc -> pc.getCategory() != null) // Avoid errors if category is null
                    .map(pc -> {
                        Category c = pc.getCategory();
                        return ProductCategoryDto.builder()
                                .categoryId(c.getCategoryId())
                                .categoryName(c.getCategoryName())
                                .categorySlug(c.getSlug())
                                .categoryIsActive(c.getIsActive())
                                // Also add product info back-reference if needed in DTO
                                .productId(p.getProductId()) // Link back to product
                                .productName(p.getProductName())
                                .build();
                    })
                    .collect(Collectors.toList());
            dto.setProductCategories(categoryDtos);
        } else {
            dto.setProductCategories(Collections.emptyList()); // Use empty list instead of null
        }

        // --- Map Images ---
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            List<ProductImageDto> imageDtos = p.getImages().stream()
                    .map(img -> ProductImageDto.builder() // Use the builder from ProductImageDto
                            .imageId(img.getImageId())
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .isPrimary(img.getIsPrimary())
                            .sortOrder(img.getSortOrder())
                            .build())
                    .collect(Collectors.toList());
            dto.setImages(imageDtos);
        } else {
            dto.setImages(Collections.emptyList()); // Use empty list instead of null
        }

        // --- Map Attribute Values ---
        if (p.getAttributeValues() != null && !p.getAttributeValues().isEmpty()) {
            List<ProductAttributeValueDto> attributeDtos = p.getAttributeValues().stream()
                    // Use the static factory method from ProductAttributeValueDto
                    .map(ProductAttributeValueDto::fromEntity)
                    .collect(Collectors.toList());
            dto.setAttributeValues(attributeDtos);
        } else {
            dto.setAttributeValues(Collections.emptyList()); // Use empty list instead of null
        }

        return dto; // Return the fully populated DTO
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
                                p.getProductId(),
                                c.getCategoryId()
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
        // 1. Find all descendant category IDs (including the category itself)
        Set<Long> categoryIdsToSearch = categoryClosureRepository.findDescendantIdsByAncestorId(categoryId);

        if (categoryIdsToSearch == null || categoryIdsToSearch.isEmpty()) {
            return Collections.emptyList(); // No descendants found (or category doesn't exist)
        }

        // 2. Find all product-category links for these IDs
        // Note: This requires a custom query or fetching all and filtering in memory.
        // Let's create a more efficient query. Add this method to ProductCategoryRepository:
        // List<ProductCategory> findByIdCategoryIdIn(Set<Long> categoryIds);
        // --- TEMPORARY IN-MEMORY FILTER (Less efficient for many products) ---
        List<ProductCategory> allProductCategories = productCategoryRepository.findAll(); // Less efficient
        List<ProductCategory> relevantProductCategories = allProductCategories.stream()
                .filter(pc -> categoryIdsToSearch.contains(pc.getCategory().getCategoryId()))
                .toList();
        // --- END TEMPORARY ---

        // If you add findByIdCategoryIdIn to ProductCategoryRepository, use this instead:
        // List<ProductCategory> relevantProductCategories = productCategoryRepository.findByIdCategoryIdIn(categoryIdsToSearch);


        if (relevantProductCategories.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Extract distinct products and map to DTOs
        return relevantProductCategories.stream()
                .map(ProductCategory::getProduct)
                .filter(Objects::nonNull) // Ensure product is not null
                .distinct() // Avoid duplicate products if linked to multiple relevant categories
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
