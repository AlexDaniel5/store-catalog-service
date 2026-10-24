package com.example.catalog.menu;

import com.example.catalog.common.web.PageResponse;
import com.example.catalog.menu.dto.MenuItemResponse;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cross-store menu search. Unlike the nested resource, the store is an optional
 * filter here, so clients can search the whole catalog by price, category, etc.
 */
@RestController
@RequestMapping("/api/v1/menu-items")
@Validated
public class MenuItemSearchController {

    private final MenuItemService menuItemService;

    public MenuItemSearchController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public PageResponse<MenuItemResponse> search(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @PositiveOrZero BigDecimal minPrice,
            @RequestParam(required = false) @PositiveOrZero BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "price") Pageable pageable) {
        var page = menuItemService.list(storeId, name, category, minPrice, maxPrice,
                available, pageable);
        return PageResponse.from(page, MenuItemResponse::from);
    }
}
