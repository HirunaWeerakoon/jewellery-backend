package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    /**
     * Use a scale suitable for currency. Precision 15, scale 2 is common:
     * max value ~ (10^(15-2)-1). Adjust if needed for your domain.
     */
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    private String imageUrl;

    private int stock;
    // getters and setters
}
