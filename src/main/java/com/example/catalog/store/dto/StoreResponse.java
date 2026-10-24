package com.example.catalog.store.dto;

import com.example.catalog.store.Store;
import java.time.Instant;

public record StoreResponse(
        Long id,
        String name,
        String description,
        String address,
        String phone,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getDescription(),
                store.getAddress(),
                store.getPhone(),
                store.isActive(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}
