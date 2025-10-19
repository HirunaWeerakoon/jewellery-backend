package com.example.jewellery_backend.dto;

import lombok.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Long categoryId;
    private String categoryName;
    private String slug;
    private Boolean isActive;

    // For hierarchical structure (optional)
    private CategoryDto parentId;                   // To reference parent category
    private List<CategoryDto> children;      // For nested subcategories

    public CategoryDto(Long categoryId, String categoryName, String slug) {
    }
}
