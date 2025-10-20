package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.Filter.FilterRequest;
import com.example.jewellery_backend.entity.Product;
import org.springframework.data.domain.Page;

public interface FilterService {
    /**
     * Filter products based on the passed FilterRequest.
     *
     * @param request FilterRequest object (minPrice, maxPrice, categoryIds, attributes, page, size)
     * @return a page of Product entities matching the filters
     */
    Page<Product> filterProducts(FilterRequest request);
}
