package com.example.catalog.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for creating or fully updating a store.
 */
public record StoreRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 1000)
        String description,

        @NotBlank
        @Size(max = 255)
        String address,

        @Size(max = 30)
        String phone,

        Boolean active
) {
}
