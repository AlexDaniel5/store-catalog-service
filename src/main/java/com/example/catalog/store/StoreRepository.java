package com.example.catalog.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StoreRepository
        extends JpaRepository<Store, Long>, JpaSpecificationExecutor<Store> {
}
