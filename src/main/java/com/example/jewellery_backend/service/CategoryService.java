package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.CategoryDto;
import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto dto);
    CategoryDto updateCategory(Long id, CategoryDto dto);
    void deleteCategory(Long id);
    CategoryDto getCategoryById(Long id);
    List<CategoryDto> getAllCategories();
}
