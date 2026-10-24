package com.example.catalog.store;

import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable {@link Specification} fragments for filtering stores. Each returns
 * {@code null} when its argument is absent, so they compose cleanly via
 * {@link Specification#allOf}.
 */
public final class StoreSpecifications {

    private StoreSpecifications() {
    }

    public static Specification<Store> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Store> hasActive(Boolean active) {
        if (active == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
}
