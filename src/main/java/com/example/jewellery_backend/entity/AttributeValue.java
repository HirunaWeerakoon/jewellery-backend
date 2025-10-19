package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Table(name = "attribute_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id")
    private Long valueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Attribute attribute;

    @Column(name = "attribute_value", nullable = false, length = 255)
    private String attributeValue;

    @OneToMany(mappedBy = "attributeValue", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<ProductAttributeValue> productAttributeLinks = new ArrayList<>();

}
