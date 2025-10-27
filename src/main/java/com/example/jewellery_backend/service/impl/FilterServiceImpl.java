package com.example.jewellery_backend.service.impl;

import com.example.jewellery_backend.dto.Filter.FilterRequest;
import com.example.jewellery_backend.entity.Product;
import com.example.jewellery_backend.repository.FilterRepository;
import com.example.jewellery_backend.repository.ProductRepository;
import com.example.jewellery_backend.service.FilterService;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class FilterServiceImpl implements FilterService {

    private final FilterRepository filterRepository;

    public FilterServiceImpl(ProductRepository productRepository, FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    @Override
    public Page<Product> filterProducts(FilterRequest request) {
        int page = (request.getPage() == null || request.getPage() < 0) ? 0 : request.getPage();
        int size = (request.getSize() == null || request.getSize() <= 0) ? 20 : request.getSize();
        Pageable pageable = PageRequest.of(page, size, Sort.by("productId").descending());

        Specification<Product> spec = buildSpecification(request);
        return filterRepository.findAll(spec, pageable);
    }

    private Specification<Product> buildSpecification(FilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!Long.class.equals(query.getResultType())) query.distinct(true);

            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), BigDecimal.valueOf(request.getMinPrice())));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), BigDecimal.valueOf(request.getMaxPrice())));
            }

            if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
                Join<Product, ?> pcJoin = root.join("productCategories", JoinType.LEFT);
                Join<?, ?> categoryJoin = pcJoin.join("category", JoinType.LEFT);
                CriteriaBuilder.In<Long> inClause = cb.in(categoryJoin.get("categoryId"));
                for (Long cid : request.getCategoryIds()) inClause.value(cid);
                predicates.add(inClause);
            }

            if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
                for (Map.Entry<String, List<String>> entry : request.getAttributes().entrySet()) {
                    String attrKey = entry.getKey();
                    List<String> values = entry.getValue();
                    if (values == null || values.isEmpty()) continue;

                    Join<Product, ?> pavJoin = root.join("attributeValues", JoinType.LEFT);
                    Join<?, ?> avJoin = pavJoin.join("attributeValue", JoinType.LEFT);
                    Join<?, ?> attrJoin = avJoin.join("attribute", JoinType.LEFT);

                    List<Predicate> keyPredicates = new ArrayList<>();
                    try {
                        Long attrId = Long.parseLong(attrKey);
                        keyPredicates.add(cb.equal(attrJoin.get("attributeId"), attrId));
                    } catch (NumberFormatException ignored) {
                        keyPredicates.add(cb.equal(cb.lower(attrJoin.get("attributeName")), attrKey.toLowerCase()));
                    }

                    List<Predicate> valuePreds = new ArrayList<>();
                    for (String v : values) {
                        if (v == null) continue;
                        String trimmed = v.trim();
                        try {
                            Long valueId = Long.parseLong(trimmed);
                            valuePreds.add(cb.equal(avJoin.get("valueId"), valueId));
                        } catch (NumberFormatException ex) {
                            valuePreds.add(cb.equal(cb.lower(avJoin.get("attributeValue")), trimmed.toLowerCase()));
                        }
                    }

                    Predicate attrKeyOrName = cb.or(keyPredicates.toArray(new Predicate[0]));
                    Predicate attrValueAny = cb.or(valuePreds.toArray(new Predicate[0]));
                    predicates.add(cb.and(attrKeyOrName, attrValueAny));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
