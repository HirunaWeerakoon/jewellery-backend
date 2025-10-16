package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
}
