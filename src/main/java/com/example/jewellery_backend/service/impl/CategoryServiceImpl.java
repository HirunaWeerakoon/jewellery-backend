package com.example.jewellery_backend.service.impl;

import com.example.jewellery_backend.dto.CategoryDto;
import com.example.jewellery_backend.entity.Category;
import com.example.jewellery_backend.exception.ResourceNotFoundException;
import com.example.jewellery_backend.repository.CategoryRepository;
import com.example.jewellery_backend.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getCategoryId(), c.getCategoryName(), c.getSlug());
    }

    private Category fromDto(CategoryDto dto) {
        Category c = new Category();
        c.setCategoryName(dto.getCategoryName());
        c.setSlug(dto.getSlug());
        return c;
    }

    @Override
    public CategoryDto createCategory(CategoryDto dto) {
        Category saved = categoryRepository.save(fromDto(dto));
        return toDto(saved);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        existing.setCategoryName(dto.getCategoryName());
        existing.setSlug(dto.getSlug());
        return toDto(categoryRepository.save(existing));
    }

    @Override
    public void deleteCategory(Long id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        categoryRepository.delete(existing);
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        return toDto(c);
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }
}
