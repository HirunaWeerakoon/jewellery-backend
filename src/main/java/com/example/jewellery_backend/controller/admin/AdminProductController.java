package com.example.jewellery_backend.controller.admin;

import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;

    @Autowired
    public AdminProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ------------------ Product Endpoints ------------------

    // Get all products
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        return productRepository.findById(productId).orElseThrow();
    }

    // Add a new product
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    // Update a product
    @PutMapping("/{productId}")
    public Product updateProduct(@PathVariable Long productId, @RequestBody Product productDetails) {
        Product product = productRepository.findById(productId).orElseThrow();

        product.setProductName(productDetails.getProductName());
        product.setSku(productDetails.getSku());
        product.setDescription(productDetails.getDescription());
        product.setBasePrice(productDetails.getBasePrice());
        product.setMarkupPercentage(productDetails.getMarkupPercentage());
        product.setWeight(productDetails.getWeight());
        product.setDimensions(productDetails.getDimensions());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setMinStockLevel(productDetails.getMinStockLevel());
        product.setIsActive(productDetails.getIsActive());
        product.setFeatured(productDetails.getFeatured());
        product.setIsGold(productDetails.getIsGold());
        product.setGoldWeightGrams(productDetails.getGoldWeightGrams());
        product.setGoldPurityKarat(productDetails.getGoldPurityKarat());

        // Optional: Update images, categories, attribute values if needed
        // product.setImages(productDetails.getImages());
        // product.setProductCategories(productDetails.getProductCategories());
        // product.setAttributeValues(productDetails.getAttributeValues());

        return productRepository.save(product);
    }

    // Delete a product
    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable Long productId) {
        productRepository.deleteById(productId);
    }

    // ------------------ Utility Methods ------------------

    // Example of correct getter usage
    public Long getProductIdExample(Product product) {
        return product.getProductId();
    }
}
