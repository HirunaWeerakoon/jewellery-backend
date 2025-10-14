package com.example.jewellery_backend.entity;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValueId implements Serializable {
    private Long product; // matches the field name in ProductAttributeValue
    private Long attributeValue; // matches the field name in ProductAttributeValue
}
