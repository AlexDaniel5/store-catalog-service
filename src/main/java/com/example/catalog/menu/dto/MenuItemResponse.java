package com.example.catalog.menu.dto;

import com.example.catalog.menu.MenuItem;
import java.math.BigDecimal;
import java.time.Instant;

public record MenuItemResponse(
        Long id,
        Long storeId,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available,
        Instant createdAt,
        Instant updatedAt
) {
    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getStore().getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory(),
                item.isAvailable(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
