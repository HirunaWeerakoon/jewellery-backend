package com.example.jewellery_backend.service.impl;

import com.example.jewellery_backend.repository.ProductImageRepository;
import com.example.jewellery_backend.dto.ProductImageDto;
import com.example.jewellery_backend.dto.ProductAttributeValueDto;
import java.util.Collections;
import com.example.jewellery_backend.dto.CreateUpdateProductRequest;
import com.example.jewellery_backend.dto.ProductDto;
import com.example.jewellery_backend.dto.ProductCategoryDto;
import com.example.jewellery_backend.entity.*;
import com.example.jewellery_backend.exception.ResourceNotFoundException;
import com.example.jewellery_backend.repository.*;
import com.example.jewellery_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import com.example.jewellery_backend.entity.GoldRate;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    private final GoldRateRepository goldRateRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUpdatedPrice(Long id) {
        Product product = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        BigDecimal basePrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
        BigDecimal markupPercentage = product.getMarkupPercentage() != null ? product.getMarkupPercentage() : BigDecimal.ZERO;
        BigDecimal materialCost = BigDecimal.ZERO;

        // Check if it's a gold product needing dynamic pricing
        if (Boolean.TRUE.equals(product.getIsGold()) &&
                product.getGoldWeightGrams() != null && product.getGoldWeightGrams().compareTo(BigDecimal.ZERO) > 0 &&
                product.getGoldPurityKarat() != null && product.getGoldPurityKarat() > 0) {

            // Fetch the latest gold rate
            Optional<GoldRate> latestRateOpt = goldRateRepository.findTopByOrderByEffectiveDateDesc();
            BigDecimal currentGoldRatePerGram = latestRateOpt.map(GoldRate::getRate).orElse(BigDecimal.ZERO);

            if (currentGoldRatePerGram.compareTo(BigDecimal.ZERO) > 0) {
                // Calculate material cost: weight * (purity/24) * rate
                BigDecimal purityFactor = BigDecimal.valueOf(product.getGoldPurityKarat()).divide(BigDecimal.valueOf(24.0), 10, RoundingMode.HALF_UP);
                materialCost = product.getGoldWeightGrams().multiply(purityFactor).multiply(currentGoldRatePerGram);
            }
            // else: if no gold rate found, material cost remains zero
        }
        // else: Not a gold item or missing gold info, material cost remains zero

        // Final price = (base_price + material_cost) * (1 + markup_percentage/100)
        BigDecimal finalPrice = basePrice.add(materialCost)
                .multiply(BigDecimal.ONE.add(markupPercentage.divide(BigDecimal.valueOf(100.0), 10, RoundingMode.HALF_UP)));

        // Return rounded to 2 decimal places (standard currency format)
        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    // ---------------- Mapping methods ----------------

    private ProductDto toDto(Product p) {
        if (p == null) return null;
        BigDecimal calculatedPrice = getUpdatedPrice(p.getProductId());
        ProductDto dto = ProductDto.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .sku(p.getSku())
                .description(p.getDescription())
                .basePrice(calculatedPrice)
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
                    .filter(pc -> pc.getCategory() != null)
                    .map(pc -> {
                        Category c = pc.getCategory();
                        return ProductCategoryDto.builder()
                                .categoryId(c.getCategoryId())
                                .categoryName(c.getCategoryName())
                                .categorySlug(c.getSlug())
                                .categoryIsActive(c.getIsActive())
                                .productId(p.getProductId())
                                .productName(p.getProductName())
                                .build();
                    })
                    .collect(Collectors.toList());
            dto.setProductCategories(categoryDtos);
        } else {
            dto.setProductCategories(Collections.emptyList());
        }

        if (p.getImages() != null && !p.getImages().isEmpty()) {
            List<ProductImageDto> imageDtos = p.getImages().stream()
                    .map(img -> ProductImageDto.builder()
                            .imageId(img.getImageId())
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .isPrimary(img.getIsPrimary())
                            .sortOrder(img.getSortOrder())
                            .build())
                    .collect(Collectors.toList());
            dto.setImages(imageDtos);
        } else {
            dto.setImages(Collections.emptyList());
        }

        if (p.getAttributeValues() != null && !p.getAttributeValues().isEmpty()) {
            List<ProductAttributeValueDto> attributeDtos = p.getAttributeValues().stream()
                    .map(ProductAttributeValueDto::fromEntity)
                    .collect(Collectors.toList());
            dto.setAttributeValues(attributeDtos);
        } else {
            dto.setAttributeValues(Collections.emptyList());
        }

        return dto;
    }

    private void applyCategories(Product p, Set<Long> newCategoryIds) {
        // Ensure the product's collection is initialized
        if (p.getProductCategories() == null) {
            p.setProductCategories(new ArrayList<>());
        }

        // Get the IDs of currently associated categories
        Set<Long> currentCategoryIds = p.getProductCategories().stream()
                .map(pc -> pc.getCategory().getCategoryId())
                .collect(Collectors.toSet());

        // Handle null input for new IDs
        Set<Long> targetCategoryIds = (newCategoryIds == null) ? Collections.emptySet() : newCategoryIds;

        // --- Remove associations no longer needed ---
        Iterator<ProductCategory> iterator = p.getProductCategories().iterator();
        while (iterator.hasNext()) {
            ProductCategory pc = iterator.next();
            // If the current category ID is NOT in the new set, remove it
            if (!targetCategoryIds.contains(pc.getCategory().getCategoryId())) {
                iterator.remove(); // Remove from the product's collection
                pc.setProduct(null); // Break bidirectional link
                pc.setCategory(null);
                // JPA/Hibernate with orphanRemoval=true should handle DB deletion
                // Or you might need: productCategoryRepository.delete(pc);
            }
        }

        // --- Add new associations ---
        for (Long newId : targetCategoryIds) {
            // If the new category ID is NOT already associated, add it
            if (!currentCategoryIds.contains(newId)) {
                Category c = categoryRepository.findById(newId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id", newId));

                ProductCategory pc = ProductCategory.builder()
                        .id(new ProductCategoryId(p.getProductId(), c.getCategoryId()))
                        .product(p)
                        .category(c)
                        .build();

                p.getProductCategories().add(pc); // Add to the managed collection
                // Saving is handled by saving the owning Product entity later
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
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .featured(req.getFeatured() != null ? req.getFeatured() : false)
                .isGold(req.getIsGold() != null ? req.getIsGold() : false)
                .goldWeightGrams(req.getGoldWeightGrams())
                .goldPurityKarat(req.getGoldPurityKarat())
                .images(new ArrayList<>())
                .productCategories(new ArrayList<>())
                .attributeValues(new ArrayList<>())
                .build();

        Product saved = productRepository.save(p);
        applyCategories(saved, req.getCategoryIds());
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            List<ProductImage> images = req.getImages().stream().map(imgDto ->
                            ProductImage.builder()
                                    .product(saved)
                                    .imageUrl(imgDto.getImageUrl())
                                    .altText(imgDto.getAltText() != null ? imgDto.getAltText() : req.getProductName()) // Default alt text
                                    .isPrimary(imgDto.getIsPrimary() != null ? imgDto.getIsPrimary() : false)
                                    .sortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : 0)
                                    .build()
            ).collect(Collectors.toList());
            saved.setImages(images); // Set the mapped images on the product
        }
        Product fullySaved = productRepository.save(saved);
        return toDto(fullySaved);
    }

    @Override
    public ProductDto updateProduct(Long id, CreateUpdateProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: "+ id));

        if (req.getProductName() != null) p.setProductName(req.getProductName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getSku() != null) p.setSku(req.getSku());
        if (req.getBasePrice() != null) p.setBasePrice(req.getBasePrice());
        if (req.getMarkupPercentage() != null) p.setMarkupPercentage(req.getMarkupPercentage());
        if (req.getWeight() != null) p.setWeight(req.getWeight());
        if (req.getDimensions() != null) p.setDimensions(req.getDimensions());
        if (req.getStockQuantity() != null) p.setStockQuantity(req.getStockQuantity());
        if (req.getMinStockLevel() != null) p.setMinStockLevel(req.getMinStockLevel());
        if (req.getIsActive() != null) p.setIsActive(req.getIsActive());
        if (req.getFeatured() != null) p.setFeatured(req.getFeatured());
        if (req.getIsGold() != null) p.setIsGold(req.getIsGold());
        if (req.getGoldWeightGrams() != null) p.setGoldWeightGrams(req.getGoldWeightGrams());
        if (req.getGoldPurityKarat() != null) p.setGoldPurityKarat(req.getGoldPurityKarat());

        applyCategories(p, req.getCategoryIds());
        if (req.getImages() != null) { // Check if images data is provided in request
            // Clear existing images managed by JPA
            if (p.getImages() == null) {
                p.setImages(new ArrayList<>());
            }
            // Use iterator to safely remove while iterating and break links
            Iterator<ProductImage> imgIterator = p.getImages().iterator();
            while (imgIterator.hasNext()) {
                ProductImage existingImg = imgIterator.next();
                imgIterator.remove(); // Remove from product's collection
                existingImg.setProduct(null); // Break bidirectional link
            }
            // Optional: If orphanRemoval=true isn't working reliably, delete explicitly
            // productImageRepository.deleteAll(imagesToRemove);


            // Add new/updated images from the request
            List<ProductImage> newImages = req.getImages().stream().map(imgDto ->
                    ProductImage.builder()
                            .product(p) // Link to the current product
                            .imageUrl(imgDto.getImageUrl())
                            .altText(imgDto.getAltText() != null ? imgDto.getAltText() : p.getProductName())
                            .isPrimary(imgDto.getIsPrimary() != null ? imgDto.getIsPrimary() : false)
                            .sortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : 0)
                            // If updating existing images based on ID, fetch existing or handle merge:
                            // .imageId(imgDto.getImageId()) // Need logic to merge/update vs create
                            .build()
            ).collect(Collectors.toList());
            p.getImages().addAll(newImages); // Add all new/updated images
        }
        Product updated = productRepository.save(p);
        return toDto(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDto(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategoryId(Long categoryId) {
        // Ensure category exists (optional but good practice)
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Get descendant category IDs including the category itself
        Set<Long> categoryIdsToSearch = categoryClosureRepository.findDescendantIdsByAncestorId(categoryId);
        if (categoryIdsToSearch == null || categoryIdsToSearch.isEmpty()) {
            return Collections.emptyList(); // No categories found
        }

        // --- CORRECTED FETCH: Use findByIdCategoryIdIn for efficiency ---
        List<ProductCategory> relevantProductCategoryLinks = productCategoryRepository.findByIdCategoryIdIn(categoryIdsToSearch);

        if (relevantProductCategoryLinks.isEmpty()) {
            return Collections.emptyList(); // No products found for these categories
        }

        // --- CORRECTED STREAM: Use the 'relevantProductCategoryLinks' variable ---
        return relevantProductCategoryLinks.stream() // <<< Use the correct variable
                .map(ProductCategory::getProduct) // Get the Product entity from the link
                .filter(Objects::nonNull)          // Filter out any null products
                .distinct()                        // Ensure each product appears only once
                .filter(p -> p.getIsActive() == null || p.getIsActive()) // Only include active products
                .map(this::toDto)                  // Convert Product entity to ProductDto (which includes calculated price)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {

        return productRepository.findById(id);
    }

    // ---------------- NEW METHOD ----------------
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getActiveProducts() {
        return productRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
