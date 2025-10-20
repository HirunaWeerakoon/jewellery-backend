package com.example.jewellery_backend.dto.Filter;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FilterRequest {
    private Double minPrice;
    private Double maxPrice;
    // list of category ids to filter
    private List<Long> categoryIds;
    // map of attribute name (or id as string) to list of attributeValue ids (or values)
    // e.g. { "Color": ["Red","Blue"], "Purity": ["22K"] } or { "1": ["2","3"] }
    private Map<String, List<String>> attributes;
    private Integer page = 0;
    private Integer size = 20;
}
