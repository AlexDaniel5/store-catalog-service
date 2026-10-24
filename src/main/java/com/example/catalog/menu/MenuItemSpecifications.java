package com.example.catalog.menu;

import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable {@link Specification} fragments for filtering menu items. Each returns
 * {@code null} when its argument is absent, so they compose cleanly via
 * {@link Specification#allOf}.
 */
public final class MenuItemSpecifications {

    private MenuItemSpecifications() {
    }

    public static Specification<MenuItem> hasStore(Long storeId) {
        if (storeId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("store").get("id"), storeId);
    }

    public static Specification<MenuItem> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<MenuItem> hasCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<MenuItem> priceAtLeast(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<MenuItem> priceAtMost(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<MenuItem> hasAvailable(Boolean available) {
        if (available == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("available"), available);
    }
}
