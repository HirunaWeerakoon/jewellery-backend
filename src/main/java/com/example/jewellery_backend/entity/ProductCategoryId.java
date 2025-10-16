package com.example.jewellery_backend.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryId implements Serializable {
    private Integer productId;
    private Integer categoryId;
}
