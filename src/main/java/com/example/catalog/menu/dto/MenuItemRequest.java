package com.example.catalog.menu.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload for creating or fully updating a menu item. The owning store is taken
 * from the request path, not the body.
 */
public record MenuItemRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        @PositiveOrZero
        @DecimalMax("99999999.99")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @Size(max = 100)
        String category,

        Boolean available
) {
}
