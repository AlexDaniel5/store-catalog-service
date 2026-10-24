package com.example.catalog.menu;

import com.example.catalog.common.error.ResourceNotFoundException;
import com.example.catalog.menu.dto.MenuItemRequest;
import com.example.catalog.menu.dto.MenuItemResponse;
import com.example.catalog.store.Store;
import com.example.catalog.store.StoreService;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final StoreService storeService;

    public MenuItemService(MenuItemRepository menuItemRepository, StoreService storeService) {
        this.menuItemRepository = menuItemRepository;
        this.storeService = storeService;
    }

    public MenuItemResponse create(Long storeId, MenuItemRequest request) {
        Store store = storeService.getEntity(storeId);
        MenuItem item = new MenuItem(
                store,
                request.name(),
                request.description(),
                request.price(),
                request.category(),
                request.available() == null || request.available());
        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public MenuItemResponse get(Long storeId, Long itemId) {
        return MenuItemResponse.from(findInStoreOrThrow(storeId, itemId));
    }

    @Transactional(readOnly = true)
    public Page<MenuItem> list(Long storeId, String name, String category,
                               BigDecimal minPrice, BigDecimal maxPrice,
                               Boolean available, Pageable pageable) {
        Specification<MenuItem> spec = Specification.allOf(
                MenuItemSpecifications.hasStore(storeId),
                MenuItemSpecifications.nameContains(name),
                MenuItemSpecifications.hasCategory(category),
                MenuItemSpecifications.priceAtLeast(minPrice),
                MenuItemSpecifications.priceAtMost(maxPrice),
                MenuItemSpecifications.hasAvailable(available));
        return menuItemRepository.findAll(spec, pageable);
    }

    public MenuItemResponse update(Long storeId, Long itemId, MenuItemRequest request) {
        MenuItem item = findInStoreOrThrow(storeId, itemId);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCategory(request.category());
        if (request.available() != null) {
            item.setAvailable(request.available());
        }
        return MenuItemResponse.from(item);
    }

    public void delete(Long storeId, Long itemId) {
        MenuItem item = findInStoreOrThrow(storeId, itemId);
        menuItemRepository.delete(item);
    }

    private MenuItem findInStoreOrThrow(Long storeId, Long itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item", itemId));
        if (!item.getStore().getId().equals(storeId)) {
            // Don't leak existence of items belonging to another store.
            throw new ResourceNotFoundException("Menu item", itemId);
        }
        return item;
    }
}
