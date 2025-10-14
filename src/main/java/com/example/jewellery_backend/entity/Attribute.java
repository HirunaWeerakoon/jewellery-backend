package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Table(name = "attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_id")
    private Long attributeId;

    @Column(name = "attribute_name", nullable = false, length = 100, unique = true)
    private String attributeName;

    // One-to-many relationship to AttributeValue
    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeValue> attributeValues = new ArrayList<>();
}
