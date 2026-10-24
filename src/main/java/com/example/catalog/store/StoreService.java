package com.example.catalog.store;

import com.example.catalog.common.error.ResourceNotFoundException;
import com.example.catalog.store.dto.StoreRequest;
import com.example.catalog.store.dto.StoreResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public StoreResponse create(StoreRequest request) {
        Store store = new Store(
                request.name(),
                request.description(),
                request.address(),
                request.phone(),
                request.active() == null || request.active());
        return StoreResponse.from(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public StoreResponse get(Long id) {
        return StoreResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<Store> list(String name, Boolean active, Pageable pageable) {
        Specification<Store> spec = Specification.allOf(
                StoreSpecifications.nameContains(name),
                StoreSpecifications.hasActive(active));
        return storeRepository.findAll(spec, pageable);
    }

    public StoreResponse update(Long id, StoreRequest request) {
        Store store = findOrThrow(id);
        store.setName(request.name());
        store.setDescription(request.description());
        store.setAddress(request.address());
        store.setPhone(request.phone());
        if (request.active() != null) {
            store.setActive(request.active());
        }
        return StoreResponse.from(store);
    }

    public void delete(Long id) {
        Store store = findOrThrow(id);
        storeRepository.delete(store);
    }

    /** Loads a store entity or throws 404; used by the menu module to resolve its owner. */
    @Transactional(readOnly = true)
    public Store getEntity(Long id) {
        return findOrThrow(id);
    }

    private Store findOrThrow(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", id));
    }
}
