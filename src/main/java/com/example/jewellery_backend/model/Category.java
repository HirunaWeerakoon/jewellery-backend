package com.example.jewellery_backend.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Table(name = "category")
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long category_id;   // ONLY @Id, no @OneToMany here

    private String category_name;

    // This is the correct place for OneToMany
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Product> products= new ArrayList<>();

    // --- Constructors ---
    public Category() {}

    public Category(String name) {
        this.category_name = name;
    }

    // --- Getters & Setters ---
    public Long getId() { return category_id; }
    public void setId(Long id) { this.category_id = id; }

    public String getName() { return category_name; }
    public void setName(String name) { this.category_name = name; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
