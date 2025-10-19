package com.example.jewellery_backend.controller.filter;

import com.example.jewellery_backend.dto.Filter.FilterRequest;
import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductFilterController {

    @Autowired
    private FilterService filterService; // ⚠️ inject instance, not call static method

    @PostMapping("/filter")
    public ResponseEntity<Page<Product>> filterProducts(@RequestBody FilterRequest filterRequest) {
        Page<Product> page = filterService.filterProducts(filterRequest);
        return ResponseEntity.ok(page);
    }
}
