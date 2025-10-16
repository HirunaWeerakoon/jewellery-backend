package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories_closure")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"ancestor", "descendant"})
public class CategoryClosure {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private CategoryClosureId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("ancestorId")
    @JoinColumn(name = "ancestor_id", nullable = false)
    private Category ancestor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("descendantId")
    @JoinColumn(name = "descendant_id", nullable = false)
    private Category descendant;

    /**
     * Distance from ancestor to descendant.
     * 0 indicates the node paired with itself; >0 for proper ancestors.
     */
    @Column(name = "depth", nullable = false)
    private Integer depth;

    // convenience factory
    public static CategoryClosure of(Category ancestor, Category descendant, Integer depth) {
        CategoryClosureId id = new CategoryClosureId(ancestor.getCategoryId(), descendant.getCategoryId());
        return CategoryClosure.builder()
                .id(id)
                .ancestor(ancestor)
                .descendant(descendant)
                .depth(depth)
                .build();
    }
}
