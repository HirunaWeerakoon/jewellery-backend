package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_attribute_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ProductAttributeValueId.class)
public class ProductAttributeValue {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "value_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AttributeValue attributeValue;
}
