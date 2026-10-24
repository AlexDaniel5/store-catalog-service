package com.example.catalog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.catalog.common.JpaConfig;
import com.example.catalog.menu.MenuItem;
import com.example.catalog.menu.MenuItemRepository;
import com.example.catalog.menu.MenuItemSpecifications;
import com.example.catalog.store.Store;
import com.example.catalog.store.StoreRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Exercises the repository layer and the filter specifications against a real
 * PostgreSQL, so Hibernate mapping and generated SQL are verified end to end.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class MenuItemRepositoryIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Store store;

    @BeforeEach
    void setUp() {
        menuItemRepository.deleteAll();
        storeRepository.deleteAll();
        store = storeRepository.save(new Store("Pizza Palace", null, "1 Main St", null, true));
        menuItemRepository.save(new MenuItem(store, "Margherita", null,
                new BigDecimal("9.50"), "Pizza", true));
        menuItemRepository.save(new MenuItem(store, "Pepperoni", null,
                new BigDecimal("12.00"), "Pizza", true));
        menuItemRepository.save(new MenuItem(store, "Cola", null,
                new BigDecimal("2.50"), "Drinks", false));
    }

    @Test
    void auditTimestampsArePopulatedOnSave() {
        MenuItem item = menuItemRepository.findAll().get(0);
        assertThat(item.getCreatedAt()).isNotNull();
        assertThat(item.getUpdatedAt()).isNotNull();
        assertThat(item.getVersion()).isNotNull();
    }

    @Test
    void filtersByPriceRange() {
        Specification<MenuItem> spec = Specification.allOf(
                MenuItemSpecifications.priceAtLeast(new BigDecimal("3.00")),
                MenuItemSpecifications.priceAtMost(new BigDecimal("10.00")));

        List<MenuItem> result = menuItemRepository.findAll(spec);

        assertThat(result).extracting(MenuItem::getName).containsExactly("Margherita");
    }

    @Test
    void filtersByAvailabilityAndCategory() {
        Specification<MenuItem> spec = Specification.allOf(
                MenuItemSpecifications.hasCategory("pizza"),
                MenuItemSpecifications.hasAvailable(true));

        List<MenuItem> result = menuItemRepository.findAll(spec);

        assertThat(result).extracting(MenuItem::getName)
                .containsExactlyInAnyOrder("Margherita", "Pepperoni");
    }

    @Test
    void filtersByStorePaged() {
        Specification<MenuItem> spec = MenuItemSpecifications.hasStore(store.getId());

        var page = menuItemRepository.findAll(spec, Pageable.ofSize(2));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
    }
}
