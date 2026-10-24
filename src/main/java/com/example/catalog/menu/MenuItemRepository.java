package com.example.catalog.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MenuItemRepository
        extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {

    boolean existsByStoreId(Long storeId);
}
