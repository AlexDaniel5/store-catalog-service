package com.example.catalog.menu;

import com.example.catalog.common.web.PageResponse;
import com.example.catalog.menu.dto.MenuItemRequest;
import com.example.catalog.menu.dto.MenuItemResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Menu items are nested under their owning store. The store id in the path is the
 * source of truth for ownership on create/update/delete.
 */
@RestController
@RequestMapping("/api/v1/stores/{storeId}/menu-items")
@Validated
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> create(@PathVariable Long storeId,
                                                   @Valid @RequestBody MenuItemRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        MenuItemResponse created = menuItemService.create(storeId, request);
        var location = uriBuilder.path("/api/v1/stores/{storeId}/menu-items/{itemId}")
                .buildAndExpand(storeId, created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{itemId}")
    public MenuItemResponse get(@PathVariable Long storeId, @PathVariable Long itemId) {
        return menuItemService.get(storeId, itemId);
    }

    @GetMapping
    public PageResponse<MenuItemResponse> list(
            @PathVariable Long storeId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @PositiveOrZero BigDecimal minPrice,
            @RequestParam(required = false) @PositiveOrZero BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        var page = menuItemService.list(storeId, name, category, minPrice, maxPrice,
                available, pageable);
        return PageResponse.from(page, MenuItemResponse::from);
    }

    @PutMapping("/{itemId}")
    public MenuItemResponse update(@PathVariable Long storeId, @PathVariable Long itemId,
                                   @Valid @RequestBody MenuItemRequest request) {
        return menuItemService.update(storeId, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long storeId, @PathVariable Long itemId) {
        menuItemService.delete(storeId, itemId);
    }
}
