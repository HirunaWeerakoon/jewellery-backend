package com.example.jewellery_backend.util;

import com.example.jewellery_backend.dto.CategoryDto;
import com.example.jewellery_backend.entity.Category;

public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        if (category == null) return null;

        return CategoryDto.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .slug(category.getSlug())
                .isActive(category.getIsActive())
                .parentId(category.getParent() != null ? toDto(category.getParent()) : null)
                .children(category.getChildren() != null
                        ? category.getChildren().stream().map(CategoryMapper::toDto).toList()
                        : null)
                .build();
    }
}
