package com.example.catalog.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.catalog.menu.MenuItemRepository;
import com.example.catalog.store.StoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full-stack test through the real HTTP layer, services, and a PostgreSQL database.
 * Covers the happy-path CRUD flow plus the error and validation paths end to end.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogApiIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @BeforeEach
    void cleanDatabase() {
        menuItemRepository.deleteAll();
        storeRepository.deleteAll();
    }

    private long createStore() throws Exception {
        String body = """
                {"name":"Pizza Palace","address":"1 Main St","phone":"555-0100"}
                """;
        String json = mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    @Test
    void fullStoreAndMenuLifecycle() throws Exception {
        long storeId = createStore();

        String itemBody = """
                {"name":"Margherita","price":9.50,"category":"Pizza","available":true}
                """;
        String itemJson = mockMvc.perform(post("/api/v1/stores/" + storeId + "/menu-items")
                        .contentType(MediaType.APPLICATION_JSON).content(itemBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storeId").value((int) storeId))
                .andReturn().getResponse().getContentAsString();
        long itemId = objectMapper.readTree(itemJson).get("id").asLong();

        mockMvc.perform(get("/api/v1/stores/" + storeId + "/menu-items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Margherita"));

        String update = """
                {"name":"Margherita","price":10.00,"category":"Pizza","available":false}
                """;
        mockMvc.perform(put("/api/v1/stores/" + storeId + "/menu-items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(10.00))
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(delete("/api/v1/stores/" + storeId + "/menu-items/" + itemId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/stores/" + storeId + "/menu-items/" + itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void listSupportsFilteringSortingAndPaging() throws Exception {
        long storeId = createStore();
        createItem(storeId, "Cola", "2.50", "Drinks", false);
        createItem(storeId, "Margherita", "9.50", "Pizza", true);
        createItem(storeId, "Pepperoni", "12.00", "Pizza", true);

        // Filter: available pizzas priced <= 10, sorted by price descending.
        mockMvc.perform(get("/api/v1/menu-items")
                        .param("storeId", String.valueOf(storeId))
                        .param("category", "Pizza")
                        .param("available", "true")
                        .param("maxPrice", "10.00")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Margherita"));

        // Paging metadata is surfaced in the wrapper.
        mockMvc.perform(get("/api/v1/menu-items")
                        .param("storeId", String.valueOf(storeId))
                        .param("size", "2")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void creatingMenuItemForMissingStoreReturns404() throws Exception {
        String itemBody = """
                {"name":"Ghost","price":1.00}
                """;
        mockMvc.perform(post("/api/v1/stores/999999/menu-items")
                        .contentType(MediaType.APPLICATION_JSON).content(itemBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Store with id 999999 not found"));
    }

    @Test
    void invalidBodyReturnsStructuredValidationError() throws Exception {
        String body = """
                {"name":"","address":""}
                """;
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='name')]").exists());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private void createItem(long storeId, String name, String price, String category,
                            boolean available) throws Exception {
        String body = """
                {"name":"%s","price":%s,"category":"%s","available":%s}
                """.formatted(name, price, category, available);
        mockMvc.perform(post("/api/v1/stores/" + storeId + "/menu-items")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }
}
