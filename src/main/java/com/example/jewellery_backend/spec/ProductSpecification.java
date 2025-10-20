package com.example.jewellery_backend.spec;

import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.entity.ProductAttributeValue;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductSpecification {

    public static Specification<Product> filterBy(Double minPrice, Double maxPrice,
                                                  List<Long> categoryIds,
                                                  Map<String, List<String>> attributes) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // price filtering (assumes Product has a 'price' field)
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // category filtering (assumes Product -> productCategories -> productCategory -> category.id)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Join<Object,Object> prodCatJoin = root.join("productCategories", JoinType.LEFT);
                predicates.add(prodCatJoin.get("category").get("id").in(categoryIds));
                // ensure distinct results
                query.distinct(true);
            }

            // attribute filtering
            if (attributes != null && !attributes.isEmpty()) {
                // join to productAttributeValues
                Join<Product, ProductAttributeValue> pavJoin = root.join("productAttributeValues", JoinType.LEFT);
                List<Predicate> attrPreds = new ArrayList<>();
                for (Map.Entry<String, List<String>> e : attributes.entrySet()) {
                    String attrKey = e.getKey();
                    List<String> vals = e.getValue();
                    // we allow attrKey to be attribute name or attribute id
                    Predicate attrMatch;
                    try {
                        Long attrId = Long.valueOf(attrKey);
                        attrMatch = cb.equal(pavJoin.get("attribute").get("id"), attrId);
                    } catch (NumberFormatException ex) {
                        attrMatch = cb.equal(pavJoin.get("attribute").get("name"), attrKey);
                    }
                    Predicate valueMatch = pavJoin.get("attributeValue").get("value").in(vals);
                    attrPreds.add(cb.and(attrMatch, valueMatch));
                }
                // Combine attribute predicates: product must match ALL attribute groups.
                // To require matching all attribute groups, we use group by product and having count.
                // Simpler approach (may produce duplicates): add them with OR across joins -- but we'll add them as AND at root level using exists subqueries would be more robust.
                // Here we add predicates that at least one of attrPreds is true. For multi-attribute 'AND' semantics, consider multiple joins or subqueries.
                predicates.add(cb.or(attrPreds.toArray(new Predicate[0])));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
